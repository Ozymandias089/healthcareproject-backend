package com.hcproj.healthcareprojectbackend.auth.dto.response;

import lombok.Builder;

@Builder
public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}
