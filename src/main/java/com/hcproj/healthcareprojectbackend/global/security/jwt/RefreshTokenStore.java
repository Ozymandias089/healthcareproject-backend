package com.hcproj.healthcareprojectbackend.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    private static String key(long userId, String jti) {
        return "rt:%d:%s".formatted(userId, jti);
    }

    public void save(long userId, String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId, jti), "1", Duration.ofSeconds(ttlSeconds));
    }

    public boolean exists(long userId, String jti) {
        Boolean hasKey = redisTemplate.hasKey(key(userId, jti));
        return Boolean.TRUE.equals(hasKey);
    }

    public void delete(long userId, String jti) {
        redisTemplate.delete(key(userId, jti));
    }
}
