package com.U100.fojbackendjudgeservice.config;

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
        serializer.setCookiePath("/api");
        serializer.setCookieMaxAge(30 * 24 * 60 * 60);   // 30 天
        serializer.setUseBase64Encoding(false);
        return serializer;
    }
}
