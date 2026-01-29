package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.config.properties.BootstrapAdminProperties;
import com.hcproj.healthcareprojectbackend.global.config.properties.ResetPasswordProperties;
import com.hcproj.healthcareprojectbackend.global.config.properties.VerificationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 인프라 공통 프로퍼티 바인딩 설정 클래스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>여러 인프라 관련 {@code @ConfigurationProperties}를 한 곳에서 활성화</li>
 *   <li>설정 전용 Config 클래스로, 별도의 Bean 정의는 하지 않음</li>
 * </ul>
 *
 * <p>
 * <b>대상 프로퍼티</b>
 * <ul>
 *   <li>{@link VerificationProperties}</li>
 *   <li>{@link ResetPasswordProperties}</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties({VerificationProperties.class, ResetPasswordProperties.class, BootstrapAdminProperties.class})
public class InfraPropertiesConfig {}
