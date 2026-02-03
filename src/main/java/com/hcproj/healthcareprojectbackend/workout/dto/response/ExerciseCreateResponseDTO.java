package com.hcproj.healthcareprojectbackend.workout.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ExerciseCreateResponseDTO(
        Long exerciseId,
        String name,
        Boolean isActive,
        String message,
        Instant createdAt
) {}