package com.U100.fojbackendgateway;

;      // ← 改成你网关的实际包路径

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration
@EnableRedisWebSession
public class RedisWebSessionConfig {
}
