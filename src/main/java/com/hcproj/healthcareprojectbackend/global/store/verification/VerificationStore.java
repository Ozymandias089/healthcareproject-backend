package com.hcproj.healthcareprojectbackend.global.store.verification;

import com.hcproj.healthcareprojectbackend.global.config.properties.VerificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 이메일 인증 관련 데이터를 Redis에 저장·조회하는 스토어.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>이메일 인증 코드 해시 저장</li>
 *   <li>인증 메일 재발송 쿨다운 관리</li>
 * </ul>
 *
 * <p>
 * <b>저장소</b>
 * <ul>
 *   <li>Redis (String 기반)</li>
 * </ul>
 *
 * <p>
 * TTL, Key Prefix 등 정책은 VerificationProperties에서 관리한다.
 */
@Component
@RequiredArgsConstructor
public class VerificationStore {

    private final StringRedisTemplate redis;
    private final VerificationProperties props;

    /**
     * 이메일 인증 코드의 해시 값을 저장한다.
     *
     * <p>
     * 원본 인증 코드는 저장하지 않고,
     * 해시 값만 저장하여 보안 위험을 최소화한다.
     *
     * @param email    대상 이메일
     * @param codeHash 인증 코드 해시
     */
    public void saveEmailCodeHash(String email, String codeHash) {
        String key = props.getEmail().getPrefix() + email;
        redis.opsForValue().set(key, codeHash, Duration.ofSeconds(props.getEmail().getTtlSeconds()));
    }

    /**
     * 이메일 인증 코드 해시를 조회한다.
     *
     * @param email 대상 이메일
     * @return 저장된 인증 코드 해시, 없으면 null
     */
    public String getEmailCodeHash(String email) {
        String key = props.getEmail().getPrefix() + email;
        return redis.opsForValue().get(key);
    }

    /**
     * 이메일 인증 코드 정보를 삭제한다.
     *
     * <p>
     * 인증 성공 후 또는 만료 처리 시 호출된다.
     *
     * @param email 대상 이메일
     */
    public void deleteEmailCode(String email) {
        String key = props.getEmail().getPrefix() + email;
        redis.delete(key);
    }

    /**
     * 인증 메일 재발송 쿨다운을 시작한다.
     *
     * <p>
     * Redis {@code SETNX} 기반으로 구현되어,
     * 이미 쿨다운이 존재하는 경우 실패한다.
     *
     * @param email 대상 이메일
     * @return 쿨다운 시작 성공 시 true, 이미 쿨다운 중이면 false
     */
    public boolean tryStartCooldown(String email) {
        String key = props.getCooldown().getPrefix() + email;
        Boolean ok = redis.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(props.getCooldown().getTtlSeconds())
        );
        return Boolean.TRUE.equals(ok);
    }
}
