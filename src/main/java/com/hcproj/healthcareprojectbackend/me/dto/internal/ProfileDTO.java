package com.hcproj.healthcareprojectbackend.me.dto.internal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProfileDTO(

        @Min(1)
        int heightCm,

        @Min(1)
        int weightKg,

        @Min(1)
        int age,

        @NotBlank
        String gender,

        @NotBlank
        String experienceLevel,

        @NotBlank
        String goalType,

        @Min(1)
        int weeklyDays,

        @Min(1)
        int sessionMinutes
) {}