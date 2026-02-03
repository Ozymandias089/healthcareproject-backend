package com.hcproj.healthcareprojectbackend.auth.social;

/**
 * 소셜 OAuth 공급자로부터 조회한 사용자 프로필 정보.
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>email은 공급자 설정/동의 범위에 따라 없을 수 있다(예: Kakao).</li>
 *   <li>providerUserId는 공급자 내에서 사용자를 식별하는 고유 값이며, 내부 계정 연동의 기준으로 사용된다.</li>
 * </ul>
 *
 * @param providerUserId 공급자 고유 사용자 ID(예: Google sub)
 * @param email          이메일(없을 수 있음)
 * @param nickname       표시 이름(없을 수 있음)
 * @param profileImageUrl 프로필 이미지 URL(없을 수 있음)
 */
public record SocialProfile(
        String providerUserId, // provider 고유 subject/id
        String email,          // 없을 수도 있음 (Kakao는 설정에 따라)
        String nickname,
        String profileImageUrl
) {}
