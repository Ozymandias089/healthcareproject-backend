package com.hcproj.healthcareprojectbackend.auth.social;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;

public interface SocialOAuthClient {
    SocialProfile fetchProfile(SocialProvider provider, String accessToken);
}
