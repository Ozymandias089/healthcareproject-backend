package com.hcproj.healthcareprojectbackend.workout.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record AiWorkoutRoutinesPutRequestDTO(
        @NotEmpty List<LocalDate> dates,
        String additionalRequest
        ) {}
