package com.hcproj.healthcareprojectbackend.global.security.jwt;

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

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // Authorization 헤더에서 accessToken 추출
            String token = resolveBearerToken(request);

            // 토큰이 없으면(=비로그인 요청) 그냥 다음 필터로 진행
            // 실제 접근 제어는 SecurityConfig에서 permitAll/authenticated로 결정됨
            if (token != null) {
                // 서명/만료 검증(실패 시 BusinessException)
                jwtTokenProvider.validate(token);

                // Authentication 생성 후 SecurityContext에 저장
                var auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            // 토큰 문제 발생 시 인증 컨텍스트 정리
            SecurityContextHolder.clearContext();

            // ErrorCode의 HTTP Status를 그대로 사용(401 등)
            response.setStatus(e.getErrorCode().status().value());
            response.setContentType("application/json;charset=UTF-8");

            // ApiResponse.fail(...) 형태로 내려주기
            // NOTE: 문자열 조합은 단순하지만, message에 특수문자/개행이 들어갈 경우 JSON 깨질 수 있음.
            //       실무에선 ObjectMapper로 직렬화하는 방식을 권장.
            String body = """
            {"success":false,"data":null,"error":{"code":"%s","message":"%s"}}
            """.formatted(e.getErrorCode().code(), e.getErrorCode().message());

            response.getWriter().write(body);
        }
    }

    /**
     * Authorization 헤더에서 Bearer 토큰을 추출한다.
     *
     * @return "Bearer "로 시작하는 헤더가 있으면 토큰 문자열, 없으면 null
     */
    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}
