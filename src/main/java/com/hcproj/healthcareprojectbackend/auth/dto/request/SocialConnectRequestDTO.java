package com.hcproj.healthcareprojectbackend.auth.dto.request;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialConnectRequestDTO(
        @NotNull SocialProvider provider,
        @NotBlank String code,
        @NotBlank String redirectUri,
        String state // NAVER용(없으면 null)
) {}