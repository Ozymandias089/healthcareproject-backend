package com.hcproj.healthcareprojectbackend.global.store.resetpassword;

import com.hcproj.healthcareprojectbackend.global.config.properties.ResetPasswordProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 비밀번호 재설정 토큰을 Redis에 저장·조회하는 스토어.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>비밀번호 재설정 토큰(ID) 관리</li>
 *   <li>토큰 유효 시간(TTL) 기반 자동 만료</li>
 * </ul>
 *
 * <p>
 * 토큰은 일회성 사용을 전제로 하며,
 * 사용 후 반드시 삭제되어야 한다.
 */
@Component
@RequiredArgsConstructor
public class PasswordResetStore {

    private final StringRedisTemplate redis;
    private final ResetPasswordProperties props;

    /**
     * 비밀번호 재설정 토큰을 저장한다.
     *
     * @param tokenId        토큰 식별자
     * @param userIdOrEmail  토큰에 매핑된 사용자 식별 정보
     */
    public void saveToken(String tokenId, String userIdOrEmail) {
        String key = props.getPrefix() + tokenId;
        redis.opsForValue().set(key, userIdOrEmail, Duration.ofSeconds(props.getTtlSeconds()));
    }

    /**
     * 비밀번호 재설정 토큰 값을 조회한다.
     *
     * @param tokenId 토큰 식별자
     * @return 매핑된 사용자 식별 정보, 없으면 null
     */
    public String getTokenValue(String tokenId) {
        String key = props.getPrefix() + tokenId;
        return redis.opsForValue().get(key);
    }

    /**
     * 비밀번호 재설정 토큰을 삭제한다.
     *
     * <p>
     * 정상 처리 시 반드시 호출되어야 하며,
     * 재사용을 방지한다.
     *
     * @param tokenId 토큰 식별자
     */
    public void deleteToken(String tokenId) {
        String key = props.getPrefix() + tokenId;
        redis.delete(key);
    }
}
