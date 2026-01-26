package com.hcproj.healthcareprojectbackend.auth.dto.internal;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import lombok.Builder;

import java.time.Instant;

@Builder
public record SocialConnectionDTO(
        SocialProvider provider,
        String providerUserId,
        Instant connectedAt
) {}
