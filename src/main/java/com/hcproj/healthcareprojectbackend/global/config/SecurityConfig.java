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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties props) {
        return new JwtTokenProvider(props);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider provider) {
        return new JwtAuthenticationFilter(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(new RestAuthEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper))
                )
                .authorizeHttpRequests(auth -> auth
                        // 개발 편의
                        .requestMatchers("/h2-console/**").permitAll()
                        // swagger (추가했을 때)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // auth는 열어둠
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(h -> h.frameOptions(f -> f.disable())); // H2 console iframe

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
