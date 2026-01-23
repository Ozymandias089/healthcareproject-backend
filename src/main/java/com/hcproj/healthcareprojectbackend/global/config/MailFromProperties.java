package com.hcproj.healthcareprojectbackend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "mail")
public class MailFromProperties {
    private String fromAddress;
    private String fromName;

}
