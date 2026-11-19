package com.U100.fojbackenduserservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/api");        // 必须保持 /api,才能同时覆盖 /api/user 和 /api/ai
        serializer.setCookieMaxAge(2592000);     // 补上:保住 30 天登录态
        serializer.setUseBase64Encoding(false);  // 核心:写裸 UUID,网关 reactive 端才能直接读
        return serializer;
    }

}
