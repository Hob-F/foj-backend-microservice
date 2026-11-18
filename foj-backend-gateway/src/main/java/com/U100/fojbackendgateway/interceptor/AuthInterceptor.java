package com.U100.fojbackendgateway.interceptor;

import com.U100.fojbackendgateway.context.UserContext;
import javax.servlet.http.*;   // ← 若你的项目是 javax.servlet(jdk8/springboot2 老体系),改成
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws IOException {
        String uid = req.getHeader("X-User-Id");
        if (uid == null || uid.isBlank()) {
            resp.setStatus(401);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"code\":40100,\"message\":\"未登录或缺少身份凭证\"}");
            return false;
        }
        UserContext.set(Long.valueOf(uid));
        return true;
    }
    @Override
    public void afterCompletion(HttpServletRequest r, HttpServletResponse s, Object h, Exception e) {
        UserContext.clear();
    }
}
