package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.config.properties.MailFromProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 메일 발신자 관련 설정을 활성화하는 구성 클래스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>메일 발신자 정보({@link MailFromProperties})를 ConfigurationProperties로 바인딩</li>
 *   <li>이메일 인증, 비밀번호 재설정 등 메일 전송 기능에서 공통 사용</li>
 * </ul>
 *
 * <p>
 * 실제 메일 전송 로직(SMTP, API)은 별도의 서비스/인프라 레이어에서 담당한다.
 */
@Configuration
@EnableConfigurationProperties(MailFromProperties.class)
public class MailConfig {}
