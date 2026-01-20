package com.hcproj.healthcareprojectbackend.me.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserProfileResponseDTO(
        int heightCm,
        int weightKg,
        int age,
        String gender,
        String experienceLevel,
        String goalType,
        int weeklyDays,
        int sessionMinutes,
        Instant createdAt,
        Instant updatedAt
) {}
