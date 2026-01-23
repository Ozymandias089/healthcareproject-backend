package com.hcproj.healthcareprojectbackend.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MailFromProperties.class)
public class MailConfig {}
