package com.hcproj.healthcareprojectbackend.diet.dto.response;

import java.math.BigDecimal;

public record FoodDetailResponseDTO(
        Long foodId,
        String name,
        String imageUrl,
        Integer calories,
        String nutritionUnit,
        Integer nutritionAmount,
        BigDecimal carbs,
        BigDecimal proteins,
        BigDecimal fats
) {}