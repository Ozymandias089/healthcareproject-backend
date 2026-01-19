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

/**
 * JWT 생성/검증/Authentication 변환을 담당하는 Provider.
 *
 * <p><b>토큰 구성(Claims)</b></p>
 * <ul>
 *   <li>sub(subject): handle (principal로 사용)</li>
 *   <li>uid: userId</li>
 *   <li>role: USER/TRAINER/ADMIN 등</li>
 *   <li>iat/exp: 발급/만료 시각</li>
 *   <li>jti: (Refresh Token에만) 토큰 고유 식별자</li>
 * </ul>
 *
 * <p><b>설계 의도</b></p>
 * <ul>
 *   <li>Access Token: 서버 저장 X, 짧은 만료 시간</li>
 *   <li>Refresh Token: jti 포함, Redis whitelist에 저장하여 로그아웃/회전/재사용 방지</li>
 * </ul>
 *
 * <p><b>예외 처리 원칙</b></p>
 * <ul>
 *   <li>만료: {@link ErrorCode#EXPIRED_TOKEN}</li>
 *   <li>위변조/형식 오류: {@link ErrorCode#INVALID_TOKEN}</li>
 * </ul>
 */
public class JwtTokenProvider {

    /** HMAC 서명 키 (HSxxx 계열) */
    private final SecretKey key;
    /** JWT 설정값(secret/만료시간) */
    private final JwtProperties props;

    /**
     * @param props app.jwt.* 설정값
     * @throws IllegalStateException secret이 너무 짧아 서명키 생성이 불가한 경우
     */
    public JwtTokenProvider(JwtProperties props) {

        // HS256/HS384/HS512 모두 "충분히 긴 secret"이 필요하다.
        // 여기서는 최소 32자(256bit 수준)를 강제해서 안전 마진을 확보.
        this.props = props;
        if (props.secret() == null || props.secret().length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters.");
        }
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token 발급.
     *
     * <p>Access Token은 Redis 저장/블랙리스트 대상이 아니므로 jti를 넣지 않는다.</p>
     */
    public String createAccessToken(Long userId, String handle, String role) {
        return createToken(userId, handle, role, props.accessTokenValiditySeconds(), null);
    }

    /**
     * Refresh Token 발급.
     *
     * <p>Refresh Token은 Redis whitelist에서 식별/폐기할 수 있어야 하므로
     * 토큰 고유 식별자(jti)를 포함한다.</p>
     *
     * @param jti refresh token 식별자(UUID 등)
     */
    public String createRefreshToken(Long userId, String handle, String role, String jti) {
        return createToken(userId, handle, role, props.refreshTokenValiditySeconds(), jti);
    }

    /**
     * JWT 생성 공통 로직.
     *
     * @param validitySeconds 만료(초)
     * @param jti null이면 jti를 포함하지 않는다(=Access Token)
     */
    private String createToken(Long userId, String handle, String role, long validitySeconds, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(validitySeconds);

        // subject(handle)를 principal로 사용한다.
        // uid/role은 API에서 user 식별 및 권한 부여에 사용.
        JwtBuilder builder = Jwts.builder()
                .subject(handle)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key);

        // Refresh Token에만 jti를 포함하여 Redis whitelist key로 사용한다.
        if (jti != null) {
            builder.id(jti); // 표준 클레임 jti
        }

        return builder.compact();
    }

    /**
     * 토큰에서 Claims를 파싱하여 반환한다.
     *
     * <p>주의: 이 메서드는 예외를 래핑하지 않는다.
     * 일반적으로는 {@link #validate(String)}를 선행하여 BusinessException으로 통일한 뒤 사용한다.</p>
     *
     * <p>Refresh Token의 uid/jti를 꺼내는 데 주로 사용.</p>
     */
    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    /**
     * 토큰 검증(서명/만료).
     *
     * @return 유효하면 true
     * @throws BusinessException 만료/위변조/형식 오류에 따라 ErrorCode를 매핑해 throw
     */
    public boolean validate(String token) {
        try {
            // 서명 검증 + 만료(exp) 검증이 동시에 수행된다.
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰: 401 AUTH-004
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            // 위변조/형식 오류 등: 401 AUTH-003
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * 토큰을 Spring Security {@link Authentication}으로 변환한다.
     *
     * <p>principal은 handle(sub)를 사용하고,
     * role claim을 "ROLE_{role}" 형태로 권한(authority)에 부여한다.</p>
     *
     * <p>추가로 uid를 details에 넣어두면
     * 컨트롤러/서비스에서 Authentication에서 userId를 꺼내 쓰기 편하다.</p>
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();

        String handle = claims.getSubject(); // principal=handle
        String role = String.valueOf(claims.get("role"));

        // Spring Security convention: "ROLE_" prefix
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        // principal = handle
        var auth = new UsernamePasswordAuthenticationToken(handle, null, authorities);

        // uid를 details로 보관 (필요 시 Controller에서 꺼내쓰기 편함)
        Object uid = claims.get("uid");
        auth.setDetails(uid);

        return auth;
    }
}
