package com.U100.fojbackendgateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

/**
 * Redis Web Session 配置。
 *
 * <p>启用 Spring Session 基于 Redis 的响应式会话管理,供网关读取用户服务写入的登录态。</p>
 *
 * @author foj-backend-gateway
 */
@Configuration
@EnableRedisWebSession
public class RedisWebSessionConfig {
}
