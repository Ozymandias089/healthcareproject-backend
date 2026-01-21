package com.hcproj.healthcareprojectbackend.workout.dto.response;

import lombok.Builder;

@Builder
public record WorkoutItemCheckResponseDTO(
        Long workoutItemId,
        Boolean isChecked
) {}