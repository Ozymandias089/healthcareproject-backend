package com.hcproj.healthcareprojectbackend.auth.dto.request;

import com.hcproj.healthcareprojectbackend.auth.entity.SocialProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequestDTO(
        @NotNull SocialProvider provider,
        @NotBlank String accessToken
) {}
