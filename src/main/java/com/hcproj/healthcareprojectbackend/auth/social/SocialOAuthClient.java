package com.hcproj.healthcareprojectbackend.auth.social;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;

public interface SocialOAuthClient {
    SocialProfile fetchProfile(SocialProvider provider, String accessToken);

    SocialProfile fetchProfileByCode(SocialProvider provider, String code, String redirectUri, String state);
}
