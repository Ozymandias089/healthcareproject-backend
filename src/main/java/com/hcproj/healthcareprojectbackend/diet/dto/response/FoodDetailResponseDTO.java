package com.hcproj.healthcareprojectbackend.diet.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FoodDetailResponseDTO(
        Long foodId,
        String name,
        String imageUrl,
        String allergies,
        Integer calories,
        String nutritionUnit,
        Integer nutritionAmount,
        BigDecimal carbs,
        BigDecimal proteins,
        BigDecimal fats
) {}