package com.hcproj.healthcareprojectbackend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 HTTP 통신을 위한 {@link RestTemplate} 설정 클래스.
 *
 * <p>
 * <b>사용 목적</b>
 * <ul>
 *   <li>소셜 로그인(OAuth) 토큰 발급 / 사용자 정보 조회</li>
 *   <li>외부 API 호출 시 공통 타임아웃 정책 적용</li>
 * </ul>
 *
 * <p>
 * <b>설정 값</b>
 * <ul>
 *   <li>{@code social.http.connect-timeout-ms}</li>
 *   <li>{@code social.http.read-timeout-ms}</li>
 * </ul>
 *
 * <p>
 * 추후 WebClient로 교체하더라도 설정 책임은 이 클래스에 유지하는 것을 권장한다.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate Bean 생성.
     *
     * @param connectTimeoutMs 연결 타임아웃 (ms)
     * @param readTimeoutMs    응답 타임아웃 (ms)
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(
            @Value("${social.http.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${social.http.read-timeout-ms:5000}") int readTimeoutMs
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
