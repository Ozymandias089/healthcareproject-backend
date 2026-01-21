package com.hcproj.healthcareprojectbackend.diet.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record DietDayResponseDTO(
        String date,
        Long dietDayId,
        List<MealDTO> meals
) {

    @Builder
    public record MealDTO(
            Long dietMealId,
            Integer sortOrder,
            List<MealItemDTO> items
    ) {}

    @Builder
    public record MealItemDTO(
            Long dietMealItemId,
            Long foodId,
            String name,
            Integer calories,
            BigDecimal carbs,
            BigDecimal proteins,
            BigDecimal fats
    ) {}
}