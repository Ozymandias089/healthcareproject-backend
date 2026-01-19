package com.hcproj.healthcareprojectbackend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 관련 설정.
 *
 * <p>Auditing(@CreatedDate, @LastModifiedDate)을 활성화한다.</p>
 *
 * <p>BaseTimeEntity에서 createdAt/updatedAt 자동 세팅을 사용하려면 필수.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {}
