package com.hcproj.healthcareprojectbackend.workout.ai.dto;

import java.time.LocalDate;
import java.util.List;

public record WorkoutAiRoutineResult(
        List<String> considerations,
        List<Day> days
) {
    public record Day(
            LocalDate logDate,
            String title,
            Integer totalMinutes,
            List<Item> items
    ) {}

    public record Item(
            Integer displayOrder,
            Long exerciseId,
            Integer sets,
            Integer reps,
            Integer restSecond,
            Integer durationMinutes,
            java.math.BigDecimal distanceKm,
            Integer rpe,
            String amount
    ) {}
}
