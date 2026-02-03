package com.hcproj.healthcareprojectbackend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
        @NotBlank String refreshToken
) {}
