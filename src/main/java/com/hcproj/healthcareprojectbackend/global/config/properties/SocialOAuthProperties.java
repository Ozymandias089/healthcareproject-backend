package com.hcproj.healthcareprojectbackend.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "social")
public class SocialOAuthProperties {

    private Google google = new Google();
    private Kakao kakao = new Kakao();
    private Naver naver = new Naver();

    @Getter @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String tokenUrl;     // https://oauth2.googleapis.com/token
        private String userinfoUrl;  // https://openidconnect.googleapis.com/v1/userinfo
    }

    @Getter @Setter
    public static class Kakao {
        private String clientId;     // REST API 키
        private String clientSecret; // 선택 (ON 했으면 필수)
        private String tokenUrl;     // https://kauth.kakao.com/oauth/token
        private String meUrl;        // https://kapi.kakao.com/v2/user/me
    }

    @Getter @Setter
    public static class Naver {
        private String clientId;
        private String clientSecret;
        private String tokenUrl;     // https://nid.naver.com/oauth2.0/token
        private String meUrl;        // https://openapi.naver.com/v1/nid/me
    }
}
