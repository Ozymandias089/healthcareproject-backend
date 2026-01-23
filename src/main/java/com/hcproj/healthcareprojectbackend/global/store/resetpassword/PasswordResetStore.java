package com.hcproj.healthcareprojectbackend.global.store.resetpassword;

import com.hcproj.healthcareprojectbackend.global.config.properties.ResetPasswordProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class PasswordResetStore {

    private final StringRedisTemplate redis;
    private final ResetPasswordProperties props;

    public PasswordResetStore(StringRedisTemplate redis, ResetPasswordProperties props) {
        this.redis = redis;
        this.props = props;
    }

    public void saveToken(String tokenId, String userIdOrEmail) {
        String key = props.getPrefix() + tokenId;
        redis.opsForValue().set(key, userIdOrEmail, Duration.ofSeconds(props.getTtlSeconds()));
    }

    public String getTokenValue(String tokenId) {
        String key = props.getPrefix() + tokenId;
        return redis.opsForValue().get(key);
    }

    public void deleteToken(String tokenId) {
        String key = props.getPrefix() + tokenId;
        redis.delete(key);
    }
}
