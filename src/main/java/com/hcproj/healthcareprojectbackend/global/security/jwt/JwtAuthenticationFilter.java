package com.hcproj.healthcareprojectbackend.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 Authorization: Bearer {accessToken}을 검사하여
 * SecurityContext에 Authentication을 세팅하는 필터.
 *
 * <p><b>중요:</b> 이 필터는 일반적으로 "Access Token"만 대상으로 한다.</p>
 * <ul>
 *   <li>Refresh Token은 재발급/로그아웃 API에서 별도로 검증한다(보통 request body/cookie 기반).</li>
 * </ul>
 *
 * <p><b>동작 흐름</b></p>
 * <ol>
 *   <li>Authorization 헤더에서 Bearer 토큰 추출</li>
 *   <li>토큰이 있으면 validate()</li>
 *   <li>getAuthentication()으로 Authentication 생성</li>
 *   <li>SecurityContext에 저장</li>
 * </ol>
 *
 * <p><b>예외 처리</b></p>
 * <ul>
 *   <li>BusinessException(토큰 만료/위변조)은 여기서 JSON으로 바로 응답</li>
 * </ul>
 *
 * <p>참고: 현재는 ObjectMapper 없이 문자열로 직접 JSON을 작성 중이다.
 * 프로젝트 표준이 ApiResponse + ObjectMapper라면 주입 방식으로 바꿔도 좋다.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // permitAll인 구간은 토큰이 있더라도 필터 자체를 스킵 (만료 토큰으로 401 나가는 문제 방지)
        if (uri.startsWith("/h2-console/")) return true;
        if (uri.startsWith("/swagger-ui/")) return true;
        if (uri.startsWith("/v3/api-docs/")) return true;
        if (uri.startsWith("/api/auth/")) return true;

        // 정확히 매칭 (뒤에 / 붙는 변형이 있다면 startsWith로 바꿔도 됨)
        if ("/api/health".equals(uri)) return true;
        if ("/api/version".equals(uri)) return true;

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = resolveBearerToken(request);

            if (token != null) {
                // validate() 제거: getAuthentication()이 검증+파싱+예외매핑까지 담당
                var auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();

            response.setStatus(e.getErrorCode().status().value());
            response.setContentType("application/json;charset=UTF-8");

            var error = new java.util.LinkedHashMap<String, Object>();
            error.put("code", e.getErrorCode().code());
            error.put("message", e.getErrorCode().message()); // null이어도 OK

            var body = new java.util.LinkedHashMap<String, Object>();
            body.put("success", false);
            body.put("data", null);
            body.put("error", error);

            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}

