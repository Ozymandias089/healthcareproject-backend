package com.hcproj.healthcareprojectbackend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 인가(Authorization) 실패 시 호출되는 Handler.
 *
 * <p>이미 인증(Authentication)은 되었지만,
 * 해당 요청에 필요한 권한(Role)이 부족한 경우 호출된다.</p>
 *
 * <p>예시:</p>
 * <ul>
 *   <li>USER 권한으로 ADMIN 전용 API 접근</li>
 *   <li>@PreAuthorize, @Secured 조건 불만족</li>
 * </ul>
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인가 실패 시 호출되는 메서드.
     *
     * @param request 현재 HTTP 요청
     * @param response HTTP 응답 객체
     * @param accessDeniedException 권한 부족 예외
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        // 권한 부족은 항상 FORBIDDEN (403)
        var ec = ErrorCode.FORBIDDEN;
        response.setStatus(ec.status().value());
        response.setContentType("application/json;charset=UTF-8");

        // ApiResponse 표준 포맷으로 에러 응답 반환
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ec.code(), ec.message()));
    }
}
