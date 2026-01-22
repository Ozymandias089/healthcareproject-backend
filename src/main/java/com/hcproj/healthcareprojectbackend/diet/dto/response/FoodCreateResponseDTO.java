package com.hcproj.healthcareprojectbackend.diet.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record FoodCreateResponseDTO(
        Long foodId,
        String name,
        Integer calories,
        Boolean isActive,
        Instant createdAt
) {}