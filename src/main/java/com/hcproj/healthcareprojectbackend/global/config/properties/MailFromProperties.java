package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 메일 발신자 정보 설정 프로퍼티.
 *
 * <p>
 * <b>설정 예</b>
 * <pre>
 * mail.from-address=no-reply@example.com
 * mail.from-name=Healthcare Project
 * </pre>
 *
 * <p>
 * <b>사용처</b>
 * <ul>
 *   <li>이메일 인증</li>
 *   <li>비밀번호 재설정 메일</li>
 * </ul>
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "mail")
public class MailFromProperties {
    /**
     * 발신자 이메일 주소
     */
    private String fromAddress;

    /**
     * 발신자 이름
     */
    private String fromName;

}
