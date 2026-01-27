package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 이메일 인증 및 재요청 쿨다운 관련 설정 프로퍼티.
 *
 * <p>
 * <b>구성</b>
 * <ul>
 *   <li>{@link Email} : 이메일 인증 토큰</li>
 *   <li>{@link Cooldown} : 인증 메일 재요청 쿨다운</li>
 * </ul>
 *
 * <p>
 * Redis 기반 토큰 저장을 전제로 설계됨.
 */
@Getter
@ConfigurationProperties(prefix = "app.verification")
public class VerificationProperties {

    private final Email email = new Email();
    private final Cooldown cooldown = new Cooldown();

    /**
     * 이메일 인증 토큰 설정
     */
    @Setter @Getter
    public static class Email {

        /** 토큰 유효 시간 (초) */
        private long ttlSeconds = 300;

        /** Redis 키 prefix */
        private String prefix = "verify:email:";

    }

    /**
     * 인증 메일 재요청 쿨다운 설정
     */
    @Setter @Getter
    public static class Cooldown {
        private long ttlSeconds = 60;
        private String prefix = "verify:cooldown:";

    }
}
