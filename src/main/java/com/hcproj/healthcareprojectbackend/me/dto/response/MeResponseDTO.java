package com.hcproj.healthcareprojectbackend.me.dto.response;

import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;

import java.time.Instant;

public record MeResponseDTO(
        Long userId,
        String email,
        String handle,
        String nickname,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        String profileImageUrl,
        Instant createdAt
) {}
