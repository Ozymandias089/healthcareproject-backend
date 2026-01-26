package com.hcproj.healthcareprojectbackend.auth.social;

public record SocialProfile(
        String providerUserId, // provider 고유 subject/id
        String email,          // 없을 수도 있음 (Kakao는 설정에 따라)
        String nickname,
        String profileImageUrl
) {}
