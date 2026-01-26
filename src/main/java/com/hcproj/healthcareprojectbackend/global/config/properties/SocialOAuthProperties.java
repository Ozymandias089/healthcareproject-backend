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
        private String userinfoUrl;
    }

    @Getter @Setter
    public static class Kakao {
        private String meUrl;
    }

    @Getter @Setter
    public static class Naver {
        private String meUrl;
    }
}
