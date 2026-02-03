package com.hcproj.healthcareprojectbackend.diet.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record FoodCreateResponseDTO(
        Long foodId,
        String name,
        Integer calories,
        BigDecimal carbs,
        BigDecimal protein,
        BigDecimal fat,
        Boolean isActive,
        String message,
        Instant createdAt
) {}