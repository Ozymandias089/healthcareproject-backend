package com.hcproj.healthcareprojectbackend.global.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PromptLoader {

    private final ResourceLoader resourceLoader;
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String loadClasspath(String relativePath) {
        return cache.computeIfAbsent(relativePath, this::readClasspath);
    }

    private String readClasspath(String relativePath) {
        try (var in = resourceLoader.getResource("classpath:" + relativePath).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Prompt load failed: " + relativePath, e);
        }
    }
}
