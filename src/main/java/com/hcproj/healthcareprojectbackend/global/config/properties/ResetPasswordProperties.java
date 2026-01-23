package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.reset-password")
public class ResetPasswordProperties {
    private long ttlSeconds = 900;
    private String prefix = "reset:pw:";

}
