package com.hcproj.healthcareprojectbackend.global.controller;

import com.hcproj.healthcareprojectbackend.global.mail.port.EmailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/dev/infra")
public class InfraDevController {

    private final EmailSender emailSender;
    private final StringRedisTemplate redis;

    public InfraDevController(EmailSender emailSender, StringRedisTemplate redis) {
        this.emailSender = emailSender;
        this.redis = redis;
    }

    @PostMapping("/mail/test")
    public Map<String, Object> mailTest(@RequestParam String to) {
        emailSender.send(to, "[TEST] Mail infra", "메일 인프라 테스트입니다.");
        return Map.of("sent", true, "to", to);
    }

    @PostMapping("/redis/test")
    public Map<String, Object> redisTest() {
        String key = "dev:test:key";
        redis.opsForValue().set(key, "ok", Duration.ofSeconds(10));
        Long ttl = redis.getExpire(key);
        return Map.of("key", key, "value", Objects.requireNonNull(redis.opsForValue().get(key)), "ttlSeconds", ttl);
    }
}
