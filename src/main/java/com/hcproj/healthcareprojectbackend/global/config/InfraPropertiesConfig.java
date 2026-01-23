package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.config.properties.ResetPasswordProperties;
import com.hcproj.healthcareprojectbackend.global.config.properties.VerificationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({VerificationProperties.class, ResetPasswordProperties.class})
public class InfraPropertiesConfig {}
