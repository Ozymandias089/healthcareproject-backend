package com.hcproj.healthcareprojectbackend.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Refresh Token을 Redis에 whitelist 방식으로 저장/관리하는 컴포넌트.
 *
 * <p>설계 의도:</p>
 * <ul>
 *   <li>Refresh Token 원문을 저장하지 않는다.</li>
 *   <li>토큰에 포함된 jti를 기준으로 "유효한 토큰인지" 여부만 관리한다.</li>
 *   <li>로그아웃/회전 시 해당 jti를 삭제하여 재발급을 차단한다.</li>
 * </ul>
 *
 * <p>Redis Key 구조:</p>
 * <pre>
 *   rt:{userId}:{jti}
 * </pre>
 *
 * <p>TTL은 Refresh Token의 만료 시간과 동일하게 설정한다.</p>
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;

    /**
     * Redis에 저장되는 Refresh Token key 생성 규칙.
     *
     * @param userId 사용자 ID
     * @param jti Refresh Token의 고유 식별자
     */
    private static String key(long userId, String jti) {
        return "rt:%d:%s".formatted(userId, jti);
    }

    /**
     * Refresh Token을 whitelist에 저장한다.
     *
     * @param userId 사용자 ID
     * @param jti Refresh Token의 고유 식별자
     * @param ttlSeconds TTL(초) - Refresh Token 만료 시간과 동일
     */
    public void save(long userId, String jti, long ttlSeconds) {
        redisTemplate.opsForValue().set(key(userId, jti), "1", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 해당 Refresh Token(jti)이 현재 유효한지 확인한다.
     *
     * @return Redis에 존재하면 true, 없으면 false
     */
    public boolean exists(long userId, String jti) {
        return redisTemplate.hasKey(key(userId, jti));
    }

    /**
     * Refresh Token을 whitelist에서 제거한다.
     *
     * <p>로그아웃 또는 재발급(회전) 시 호출된다.</p>
     * <p>idempotent 하게 동작하므로,
     * 이미 삭제된 토큰을 다시 삭제해도 문제없다.</p>
     */
    public void delete(long userId, String jti) {
        redisTemplate.delete(key(userId, jti));
    }
}
