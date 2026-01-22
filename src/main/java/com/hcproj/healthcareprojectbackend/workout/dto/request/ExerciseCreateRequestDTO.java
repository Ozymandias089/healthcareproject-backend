package com.hcproj.healthcareprojectbackend.workout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseCreateRequestDTO(
        @NotBlank String name,
        String bodyPart,
        String difficulty,
        String imageUrl,
        @NotBlank String description,
        String precautions,
        String youtubeUrl,
        @NotNull Boolean isActive
) {}