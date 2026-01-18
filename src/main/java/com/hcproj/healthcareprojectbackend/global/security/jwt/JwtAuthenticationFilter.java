package com.hcproj.healthcareprojectbackend.global.security.jwt;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
            String token = resolveBearerToken(request);
            if (token != null) {
                jwtTokenProvider.validate(token);
                var auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);

        } catch (BusinessException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(e.getErrorCode().status().value());
            response.setContentType("application/json;charset=UTF-8");

            // ApiResponse.fail(...) 형태로 내려주기 (ObjectMapper 주입 방식도 OK)
            String body = """
            {"success":false,"data":null,"error":{"code":"%s","message":"%s"}}
            """.formatted(e.getErrorCode().code(), e.getErrorCode().message());

            response.getWriter().write(body);
        }
    }


    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}
