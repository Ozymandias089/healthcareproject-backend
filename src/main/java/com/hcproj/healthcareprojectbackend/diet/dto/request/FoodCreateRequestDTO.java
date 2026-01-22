package com.hcproj.healthcareprojectbackend.diet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FoodCreateRequestDTO(
        @NotBlank String name,
        String imageUrl,
        @NotBlank String nutritionUnit,
        @NotNull Integer nutritionAmount,
        @NotNull Integer calories,
        BigDecimal carbs,
        BigDecimal protein,
        BigDecimal fat,
        String displayServing,
        String allergyCodes,
        @NotNull Boolean isActive
) {}