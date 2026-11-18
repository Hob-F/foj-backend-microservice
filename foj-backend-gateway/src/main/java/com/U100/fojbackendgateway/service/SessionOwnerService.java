package com.U100.fojbackendgateway.service;

import com.U100.fojbackendgateway.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionOwnerService {
    private final StringRedisTemplate redisTemplate;
    public SessionOwnerService(StringRedisTemplate redisTemplate) { this.redisTemplate = redisTemplate; }

    public void bindOwner(String sessionId, Long userId) {
        if (userId == null) throw new BizException(40100, "未登录");
        String key = "ai:session:owner:" + sessionId;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(userId));
        if (Boolean.FALSE.equals(first)) {
            String owner = redisTemplate.opsForValue().get(key);
            if (owner == null || !owner.equals(String.valueOf(userId))) {
                throw new BizException(40300, "无权访问该会话");
            }
        }
    }
}
