package com.U100.fojbackendgateway.filter;

import com.U100.fojbackendcommon.constant.UserConstant;
import com.U100.fojbackendmodel.model.entity.User;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * AI 路由登录态校验过滤器。
 *
 * <p>拦截 {@code /api/ai/**}:通过 Spring Session 读取 Redis 中的登录态,
 * 未登录返回 401,已登录则把用户 ID 注入可信头 {@code X-User-Id} 后转发给 AI 服务。</p>
 *
 * @author foj-backend-gateway
 */
@Component
public class AiAuthFilter implements GlobalFilter, Ordered {

    private final ReactiveSessionRepository<? extends Session> repository;

    public AiAuthFilter(ReactiveSessionRepository<? extends Session> repository,
                        ReactiveRedisConnectionFactory factory,
                        RedisConnectionFactory servletFactory) {
        this.repository = repository;
        if (factory instanceof LettuceConnectionFactory) {

        }
        if (servletFactory instanceof LettuceConnectionFactory) {

        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/api/ai/")) {
            return chain.filter(exchange);
        }
        // 用 Spring Session 的 WebSession 解析:自动完成 cookie base64 解码 + Redis 会话加载

        return exchange.getSession()
                .flatMap(session -> {

                    Object state = session.getAttribute(UserConstant.USER_LOGIN_STATE);

                    if (!(state instanceof User)) {
                        return unauthorized(exchange);
                    }
                    ServerHttpRequest req = exchange.getRequest().mutate()
                            .header("X-User-Id", String.valueOf(((User) state).getId()))
                            .build();
                    return chain.filter(exchange.mutate().request(req).build());
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
