package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app.verification")
public class VerificationProperties {

    private final Email email = new Email();
    private final Cooldown cooldown = new Cooldown();

    @Getter
    public static class Email {
        private long ttlSeconds = 300;
        private String prefix = "verify:email:";

        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }

        public void setPrefix(String prefix) { this.prefix = prefix; }
    }

    @Getter
    public static class Cooldown {
        private long ttlSeconds = 60;
        private String prefix = "verify:cooldown:";

        public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }

        public void setPrefix(String prefix) { this.prefix = prefix; }
    }
}
