package com.hcproj.healthcareprojectbackend.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 유저 단위 Refresh Token "전체 무효화"를 위한 버전 저장소.
 * <p>
 * Key: rtv:{userId} -> int (문자열)
 * <p>
 * - refresh 발급 시 토큰에 ver 클레임을 넣는다.
 * - 재발급 시 토큰 ver == 현재 ver 일치 여부를 검사한다.
 * - 탈퇴/전체 로그아웃 시 ver를 증가시키면 기존 refresh는 전부 무효화된다.
 */
@Service
@RequiredArgsConstructor
public class TokenVersionStore {

    private final StringRedisTemplate redisTemplate;

    private static String key(long userId) {
        return "rtv:%d".formatted(userId);
    }

    /**
     * 현재 버전 조회.
     * 값이 없다면 초기값(예: 1)을 세팅하고 반환한다.
     */
    public int getOrInit(long userId) {
        String k = key(userId);
        String v = redisTemplate.opsForValue().get(k);
        if (v != null) return Integer.parseInt(v);

        // 초기 버전은 1로 시작(0도 가능하지만 보통 1이 가독성 좋음)
        redisTemplate.opsForValue().set(k, "1");
        return 1;
    }

    /**
     * 버전 증가(전부 무효화 스위치).
     * - 탈퇴/전체 로그아웃 등에 사용
     */
    public int bump(long userId) {
        String k = key(userId);
        Long newVal = redisTemplate.opsForValue().increment(k);
        if (newVal == null) {
            redisTemplate.opsForValue().set(k, "2");
            return 2;
        }
        return newVal.intValue();
    }

    /**
     * 버전에 TTL을 주고 싶으면 사용.
     * 보통은 유저 단위 키라 TTL 없이 두는 편이 간단함.
     */
    public void expire(long userId, Duration ttl) {
        redisTemplate.expire(key(userId), ttl);
    }
}
