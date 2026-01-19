package com.hcproj.healthcareprojectbackend.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.security.RestAccessDeniedHandler;
import com.hcproj.healthcareprojectbackend.global.security.RestAuthEntryPoint;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtAuthenticationFilter;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtProperties;
import com.hcproj.healthcareprojectbackend.global.security.jwt.JwtTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정.
 *
 * <p><b>보안 정책 요약</b></p>
 * <ul>
 *   <li>세션 미사용(STATELESS): JWT 기반 인증</li>
 *   <li>CSRF 비활성화: API 서버 + JWT 조합</li>
 *   <li>/api/auth/** 는 인증 없이 접근 가능(회원가입/로그인/재발급/로그아웃 등)</li>
 *   <li>그 외 모든 API는 인증 필요</li>
 * </ul>
 *
 * <p><b>예외 응답 표준화</b></p>
 * <ul>
 *   <li>인증 실패(401): {@link RestAuthEntryPoint}</li>
 *   <li>권한 부족(403): {@link RestAccessDeniedHandler}</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * JwtTokenProvider 빈 등록.
     *
     * <p>JwtProperties(app.jwt.*)를 주입받아 secret/만료시간을 설정한다.</p>
     */
    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties props) {
        return new JwtTokenProvider(props);
    }

    /**
     * Authorization: Bearer {accessToken}을 검사하는 JWT 인증 필터 빈 등록.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider) {
        return new JwtAuthenticationFilter(provider);
    }

    /**
     * SecurityFilterChain 구성.
     *
     * <p>핵심:</p>
     * <ul>
     *   <li>STATELESS</li>
     *   <li>인증/인가 예외 응답을 ApiResponse로 통일</li>
     *   <li>JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                // API 서버 + JWT 조합에서는 보통 CSRF 미사용
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 기본 설정(필요 시 CorsConfigurationSource로 커스터마이징)
                .cors(Customizer.withDefaults())

                // 세션 기반 인증 미사용(JWT는 요청마다 검증)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증/인가 예외를 표준 응답(ApiResponse)로 변환
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new RestAuthEntryPoint(objectMapper))      // 401
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper))      // 403
                )

                // 요청별 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // 개발 편의: H2 콘솔 접근 허용
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger UI 및 OpenAPI 문서(추가했을 때)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 인증/회원 관련 API는 인증 없이 접근 가능
                        .requestMatchers("/api/auth/**").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // H2 콘솔 iframe 사용을 위해 frameOptions 비활성화
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // JWT 필터는 UsernamePasswordAuthenticationFilter 이전에 두는 것이 일반적
        // (기본 폼 로그인 인증 필터보다 먼저 토큰 인증을 처리)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 해시용 인코더.
     *
     * <p>평문 비밀번호 저장 금지. BCrypt는 솔트 포함 + 느린 해시로 안전성이 높다.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
