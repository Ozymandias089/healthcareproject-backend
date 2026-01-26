package com.hcproj.healthcareprojectbackend.global.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AiJsonCaller {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public <T> T callJson(String systemPrompt, String userPrompt, Class<T> clazz) {
        String raw = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        try {
            String cleaned = stripCodeFences(Objects.requireNonNull(raw));
            return objectMapper.readValue(cleaned, clazz);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_JSON_PARSE_FAILED);
        }
    }

    private String stripCodeFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            // ```json\n{...}\n``` 형태 제거
            int firstNewline = t.indexOf('\n');
            int lastFence = t.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                return t.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return t;
    }
}
