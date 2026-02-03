package com.hcproj.healthcareprojectbackend.global.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * AI(ChatGPT 등 LLM) 호출 결과를 JSON으로 받아
 * 지정한 DTO(Class)로 역직렬화하는 공통 유틸 컴포넌트.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>Spring AI {@link ChatClient}를 이용해 시스템 프롬프트 + 유저 프롬프트를 호출한다.</li>
 *   <li>LLM 응답에서 Markdown 코드 펜스(```json ... ```)를 제거한다.</li>
 *   <li>정제된 JSON 문자열을 Jackson {@link ObjectMapper}로 파싱한다.</li>
 * </ul>
 *
 * <p>
 * <b>사용 의도</b>
 * <ul>
 *   <li>AI 응답을 "문자열"이 아닌 "구조화된 JSON DTO"로 안전하게 사용하기 위함</li>
 *   <li>AI 인프라 레이어에서 JSON 파싱 책임을 중앙화</li>
 * </ul>
 *
 * <p>
 * <b>에러 처리</b>
 * <ul>
 *   <li>JSON 파싱 실패 시 {@link BusinessException}({@link ErrorCode#AI_JSON_PARSE_FAILED}) 발생</li>
 *   <li>상위 레이어에서는 AI 응답 신뢰 실패로 처리한다.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AiJsonCaller {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * AI를 호출하고 JSON 응답을 지정한 타입으로 변환한다.
     *
     * @param systemPrompt 시스템 프롬프트 (역할, 규칙 정의)
     * @param userPrompt   사용자 프롬프트 (실제 요청 내용)
     * @param clazz        역직렬화 대상 클래스
     * @param <T>          응답 DTO 타입
     * @return JSON을 파싱한 DTO 객체
     * @throws BusinessException JSON 파싱 실패 시
     */
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

    /**
     * LLM 응답에 포함된 Markdown 코드 펜스(```json ... ```)를 제거한다.
     *
     * <p>
     * 예:
     * <pre>
     * ```json
     * { "key": "value" }
     * ```
     * </pre>
     *
     * @param s 원본 문자열
     * @return 코드 펜스가 제거된 JSON 문자열
     */
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
