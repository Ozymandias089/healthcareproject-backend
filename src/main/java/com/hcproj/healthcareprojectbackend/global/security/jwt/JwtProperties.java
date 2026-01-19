package com.hcproj.healthcareprojectbackend.global.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정값.
 *
 * <p>application.properties/yml 예시:</p>
 * <pre>
 * app.jwt.secret=...
 * app.jwt.access-token-validity-seconds=3600
 * app.jwt.refresh-token-validity-seconds=1209600
 * </pre>
 *
 * <p><b>권장</b></p>
 * <ul>
 *   <li>secret은 충분히 길고 랜덤해야 한다(최소 32자 이상).</li>
 *   <li>Access Token은 짧게(예: 15~60분), Refresh Token은 길게(예: 7~14일).</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds
) {}