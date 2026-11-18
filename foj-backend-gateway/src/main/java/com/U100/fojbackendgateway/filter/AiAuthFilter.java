package com.U100.fojbackendgateway.filter;          // ← 改成你 gateway 实际包路径

import com.U100.fojbackendmodel.model.entity.User;        // ← 打开 model 模块的 User 类,抄它的完整包路径
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AiAuthFilter implements GlobalFilter, Ordered {

    private static final String LOGIN_KEY = "userLoginState";

    private final ReactiveSessionRepository<? extends Session> repository;

    public AiAuthFilter(ReactiveSessionRepository<? extends Session> repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/api/ai/")) {                 // 只拦 AI 路由,其余放行
            return chain.filter(exchange);
        }
        String cookieValue = exchange.getRequest().getCookies().getFirst("SESSION") == null
                ? null : exchange.getRequest().getCookies().getFirst("SESSION").getValue();
        if (cookieValue == null) {
            return unauthorized(exchange);
        }
        return repository.findById(cookieValue)
                .flatMap(session -> {
                    Object state = session.getAttribute(LOGIN_KEY);          // ← 改成这样
                    if (!(state instanceof User)) {
                        return unauthorized(exchange);
                    }
                    ServerHttpRequest req = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(((User) state).getId()))
                            .build();
                    return chain.filter(exchange.mutate().request(req).build());

                })
                .onErrorResume(e -> unauthorized(exchange))          // session 数据损坏等异常
                .switchIfEmpty(Mono.defer(() -> unauthorized(exchange))); // 过期/不存在
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() { return -100; }
}
