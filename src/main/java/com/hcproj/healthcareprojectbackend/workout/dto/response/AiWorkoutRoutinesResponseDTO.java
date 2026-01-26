package com.hcproj.healthcareprojectbackend.workout.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AiWorkoutRoutinesResponseDTO(
        Instant generatedAt,
        PlanSummary planSummary,
        List<String> considerations,
        List<Day> days
) {
    public record PlanSummary(int rangeDays, int workoutDayCount) {}

    public record Day(
            Long workoutDayId,
            LocalDate logDate,
            String dayOfWeek,
            String title,
            Integer totalMinutes,
            List<Item> items
    ) {}

    public record Item(
            Long workoutItemId,
            Integer displayOrder,
            Long exerciseId,
            String exerciseName,
            Integer sets,
            Integer reps,
            Integer restSecond,
            Integer durationMinutes,
            BigDecimal distanceKm,
            Integer rpe,
            String amount,
            Boolean isChecked
    ) {}
}
