package com.hcproj.healthcareprojectbackend.me.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record MeResponseDTO(
        String email,
        String handle,
        String nickname,
        String role,
        String status,
        String profileImageUrl,
        String phoneNumber,
        Instant createdAt,
        Instant updatedAt
) {}
