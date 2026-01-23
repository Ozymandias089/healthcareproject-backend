package com.hcproj.healthcareprojectbackend.global.store.verification;

import com.hcproj.healthcareprojectbackend.global.config.properties.VerificationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class VerificationStore {

    private final StringRedisTemplate redis;
    private final VerificationProperties props;

    public VerificationStore(StringRedisTemplate redis, VerificationProperties props) {
        this.redis = redis;
        this.props = props;
    }

    public void saveEmailCodeHash(String email, String codeHash) {
        String key = props.getEmail().getPrefix() + email;
        redis.opsForValue().set(key, codeHash, Duration.ofSeconds(props.getEmail().getTtlSeconds()));
    }

    public String getEmailCodeHash(String email) {
        String key = props.getEmail().getPrefix() + email;
        return redis.opsForValue().get(key);
    }

    public void deleteEmailCode(String email) {
        String key = props.getEmail().getPrefix() + email;
        redis.delete(key);
    }

    /** 재발송 쿨다운(있으면 실패 처리) */
    public boolean tryStartCooldown(String email) {
        String key = props.getCooldown().getPrefix() + email;
        Boolean ok = redis.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(props.getCooldown().getTtlSeconds())
        );
        return Boolean.TRUE.equals(ok);
    }
}
