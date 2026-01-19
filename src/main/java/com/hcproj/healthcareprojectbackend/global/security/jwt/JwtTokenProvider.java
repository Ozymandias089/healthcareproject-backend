package com.hcproj.healthcareprojectbackend.global.security.jwt;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class JwtTokenProvider {

    private final SecretKey key;
    private final JwtProperties props;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        if (props.secret() == null || props.secret().length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters.");
        }
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String handle, String role) {
        return createToken(userId, handle, role, props.accessTokenValiditySeconds(), null);
    }

    public String createRefreshToken(Long userId, String handle, String role, String jti) {
        return createToken(userId, handle, role, props.refreshTokenValiditySeconds(), jti);
    }

    private String createToken(Long userId, String handle, String role, long validitySeconds, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(validitySeconds);

        JwtBuilder builder = Jwts.builder()
                .subject(handle)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key);

        if (jti != null) {
            builder.id(jti); // ✅ 표준 클레임 jti
        }

        return builder.compact();
    }

    /** refreshToken에서 jti/uid 뽑을 때 편하게 */
    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        String handle = claims.getSubject(); // ✅ principal=handle
        String role = String.valueOf(claims.get("role"));

        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // principal = handle
        var auth = new UsernamePasswordAuthenticationToken(handle, null, authorities);

        // (선택) uid를 details로 보관하면 꺼내쓰기 쉬움
        Object uid = claims.get("uid");
        auth.setDetails(uid); // Long로 들어옴(파싱 필요할 수도 있음)

        return auth;
    }
}
