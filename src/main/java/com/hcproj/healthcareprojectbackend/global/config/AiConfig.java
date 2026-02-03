package com.hcproj.healthcareprojectbackend.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI(ChatClient) 관련 공통 설정 클래스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>Spring AI의 {@link ChatClient}를 애플리케이션 전역에서 공통으로 사용하기 위한 Bean 설정</li>
 *   <li>하위 레이어(AI 인프라, 서비스 등)에서는 ChatModel 구현체에 직접 의존하지 않도록 분리</li>
 * </ul>
 *
 * <p>
 * <b>설계 의도</b>
 * <ul>
 *   <li>{@link ChatModel}은 환경(OpenAI, Azure, Local 등)에 따라 교체 가능</li>
 *   <li>애플리케이션 코드는 ChatClient만 의존하도록 하여 확장성 확보</li>
 * </ul>
 */
@Configuration
public class AiConfig {

    /**
     * {@link ChatClient} Bean 생성.
     *
     * @param chatModel 실제 LLM 호출을 담당하는 ChatModel 구현체
     * @return ChatClient 인스턴스
     */
    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
