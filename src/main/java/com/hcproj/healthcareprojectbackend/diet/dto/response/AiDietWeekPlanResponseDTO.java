package com.hcproj.healthcareprojectbackend.diet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AiDietWeekPlanResponseDTO(
        LocalDate startDate,
        LocalDate endDate,
        List<String> considerations,
        List<Day> days,
        PageInfo pageInfo
) {
    public record PageInfo(int days) {}

    public record Day(
            Long dietDayId,
            LocalDate logDate,
            Summary summary,
            List<Meal> meals
    ) {}

    public record Summary(
            Integer totalCalories,
            Integer mealCount
    ) {}

    public record Meal(
            Long dietMealId,
            Integer displayOrder,
            Boolean isChecked,
            String title,
            Nutrition nutrition,
            List<Item> items
    ) {}

    public record Nutrition(
            Integer calories,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat
    ) {}

    public record Item(
            Long dietMealItemId,
            Long foodId,
            String name,
            String imageUrl,
            String nutritionUnit,
            Integer nutritionAmount,
            Integer grams,        // 현재 모델엔 없음 → null로 내려줌
            Integer count,
            Boolean isChecked
    ) {}
}
