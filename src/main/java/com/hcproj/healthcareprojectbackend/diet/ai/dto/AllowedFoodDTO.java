package com.hcproj.healthcareprojectbackend.diet.ai.dto;

public record AllowedFoodDTO(
        Long id,
        String name,
        Integer calories,
        String nutritionUnit,
        Integer nutritionAmount
) {}
