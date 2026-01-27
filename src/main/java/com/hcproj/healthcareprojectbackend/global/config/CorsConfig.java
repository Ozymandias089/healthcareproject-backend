package com.hcproj.healthcareprojectbackend.global.config;

import com.hcproj.healthcareprojectbackend.global.config.properties.CorsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS(Cross-Origin Resource Sharing) 전역 설정 클래스.
 *
 * <p>
 * <b>역할</b>
 * <ul>
 *   <li>프론트엔드와 백엔드 간의 교차 출처 요청 허용 정책 정의</li>
 *   <li>환경별 Origin 설정을 {@link CorsProperties}로 분리하여 관리</li>
 * </ul>
 *
 * <p>
 * <b>적용 범위</b>
 * <ul>
 *   <li>모든 API 경로("/**")에 공통 적용</li>
 *   <li>Spring Security 설정에서 {@code corsConfigurationSource()}로 참조됨</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    /**
     * CORS 설정 소스 Bean.
     *
     * @param props CORS 관련 설정 프로퍼티
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration config = new CorsConfiguration();

        /// 허용 Origin 목록
        config.setAllowedOrigins(props.getAllowedOrigins());

        /// 허용 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        /// 허용 헤더
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        /*
          JWT Authorization 헤더 사용을 위해 true 설정
          (추후 쿠키 기반 인증 확장 가능성 고려)
         */
        config.setAllowCredentials(true);

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
