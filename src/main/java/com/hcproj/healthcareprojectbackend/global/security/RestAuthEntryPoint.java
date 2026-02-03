package com.hcproj.healthcareprojectbackend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 인증(Authentication) 실패 시 호출되는 EntryPoint.
 *
 * <p>Spring Security 필터 체인에서 인증이 필요하지만
 * Authentication 객체가 존재하지 않거나 유효하지 않을 경우 호출된다.</p>
 *
 * <p>예시:</p>
 * <ul>
 *   <li>Authorization 헤더가 없는 상태로 보호된 API 호출</li>
 *   <li>Access Token이 없거나 잘못된 형식인 경우</li>
 * </ul>
 *
 * <p>모든 인증 실패 응답을 ApiResponse 표준 형식으로 통일한다.</p>
 */
public class RestAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패 시 호출되는 메서드.
     *
     * @param request  현재 HTTP 요청
     * @param response HTTP 응답 객체
     * @param authException Spring Security 내부 인증 예외
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        // 인증 실패는 항상 UNAUTHORIZED (401)로 통일
        var ec = ErrorCode.UNAUTHORIZED;
        response.setStatus(ec.status().value());
        response.setContentType("application/json;charset=UTF-8");

        // ApiResponse 표준 포맷으로 에러 응답 반환
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ec.code(), ec.message()));
    }
}
