package com.hcproj.healthcareprojectbackend.auth.social;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;

/**
 * 소셜 OAuth 공급자(Google/Kakao/Naver)로부터 사용자 프로필을 조회하기 위한 포트(Port).
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>외부 OAuth Provider API 호출을 추상화하여 도메인/서비스 레이어가 HTTP 세부사항에 의존하지 않게 한다.</li>
 *   <li>공급자별 차이(엔드포인트/필드명/토큰 교환 방식)를 구현체(Adapter)에서 캡슐화한다.</li>
 * </ul>
 *
 * <p><b>입력/출력 계약</b></p>
 * <ul>
 *   <li>{@link #fetchProfile(SocialProvider, String)}: Access Token을 이용해 프로필을 조회한다.</li>
 *   <li>{@link #fetchProfileByCode(SocialProvider, String, String, String)}: Authorization Code를 Access Token으로 교환 후 프로필을 조회한다.</li>
 * </ul>
 */
public interface SocialOAuthClient {

    /**
     * Access Token으로 소셜 사용자 프로필을 조회한다.
     *
     * @param provider    소셜 공급자
     * @param accessToken OAuth Access Token (Bearer)
     * @return 공급자 사용자 프로필
     */
    SocialProfile fetchProfile(SocialProvider provider, String accessToken);

    /**
     * Authorization Code로 Access Token을 교환한 뒤 소셜 사용자 프로필을 조회한다.
     *
     * <p>
     * Provider에 따라 {@code state}가 필요하거나 권장될 수 있다(예: Naver).
     * </p>
     *
     * @param provider    소셜 공급자
     * @param code        Authorization Code
     * @param redirectUri 등록된 Redirect URI
     * @param state       CSRF 방지용 state (provider별 정책에 따라 필수/권장)
     * @return 공급자 사용자 프로필
     */
    SocialProfile fetchProfileByCode(SocialProvider provider, String code, String redirectUri, String state);
}
