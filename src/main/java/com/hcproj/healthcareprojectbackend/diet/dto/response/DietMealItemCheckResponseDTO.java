package com.hcproj.healthcareprojectbackend.diet.dto.response;

import lombok.Builder;

@Builder
public record DietMealItemCheckResponseDTO(
        String message,
        Long dietMealItemId,
        Boolean checked
) {}