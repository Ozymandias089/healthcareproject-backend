package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 비밀번호 재설정 토큰 설정 프로퍼티.
 *
 * <p>
 * <b>설정 예</b>
 * <pre>
 * app.reset-password.ttl-seconds=900
 * app.reset-password.prefix=reset:pw:
 * </pre>
 *
 * <p>
 * <b>설명</b>
 * <ul>
 *   <li>Redis 등에 저장되는 비밀번호 재설정 토큰의 TTL 관리</li>
 *   <li>prefix를 통해 키 충돌 방지</li>
 * </ul>
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "app.reset-password")
public class ResetPasswordProperties {
    /**
     * 토큰 유효 시간 (초)
     * 기본값: 15분
     */
    private long ttlSeconds = 900;

    /**
     * Redis 키 prefix
     */
    private String prefix = "reset:pw:";
}
