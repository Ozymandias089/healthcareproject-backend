package com.hcproj.healthcareprojectbackend.diet.ai.dto;

import java.time.LocalDate;
import java.util.List;

public record DietAiWeekPlanResult(
        LocalDate startDate,
        LocalDate endDate,
        List<Day> days
) {
    public record Day(LocalDate logDate, List<Meal> meals) {}
    public record Meal(Integer displayOrder, String title, List<Item> items) {}
    public record Item(Long foodId, Integer count) {}
}
