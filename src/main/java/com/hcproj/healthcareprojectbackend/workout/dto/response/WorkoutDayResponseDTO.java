package com.hcproj.healthcareprojectbackend.workout.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record WorkoutDayResponseDTO(
        String date,
        Long workoutDayId,
        String title,
        Integer totalMinutes,
        Integer exerciseCount,
        Integer completedCount,
        List<WorkoutItemDTO> items
) {
    @Builder
    public record WorkoutItemDTO(
            Long workoutItemId,
            Long exerciseId,
            String name,
            Integer restSeconds,
            String amount,
            Integer rpe,
            Boolean isChecked,
            Integer sortOrder
    ) {}
}