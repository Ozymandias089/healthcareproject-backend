package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 소셜 로그인(OAuth) 제공자별 설정 프로퍼티.
 *
 * <p>
 * <b>지원 제공자</b>
 * <ul>
 *   <li>Google</li>
 *   <li>Kakao</li>
 *   <li>Naver</li>
 * </ul>
 *
 * <p>
 * <b>설계 의도</b>
 * <ul>
 *   <li>소셜 제공자별 설정을 하나의 루트로 통합</li>
 *   <li>향후 Apple, Facebook 등 확장 용이</li>
 * </ul>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "social")
public class SocialOAuthProperties {

    private Google google = new Google();
    private Kakao kakao = new Kakao();
    private Naver naver = new Naver();

    /** Google OAuth 설정 */
    @Getter @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String tokenUrl;     // https://oauth2.googleapis.com/token
        private String userinfoUrl;  // https://openidconnect.googleapis.com/v1/userinfo
    }

    /** Kakao OAuth 설정 */
    @Getter @Setter
    public static class Kakao {
        private String clientId;     // REST API 키
        private String clientSecret; // 선택 (ON 했으면 필수)
        private String tokenUrl;     // https://kauth.kakao.com/oauth/token
        private String meUrl;        // https://kapi.kakao.com/v2/user/me
    }

    /** Naver OAuth 설정 */
    @Getter @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
        private String tokenUrl;     // https://nid.naver.com/oauth2.0/token
        private String meUrl;        // https://openapi.naver.com/v1/nid/me
    }
}
