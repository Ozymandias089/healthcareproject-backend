package com.hcproj.healthcareprojectbackend.diet.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record FoodListResponseDTO(
        List<FoodItemDTO> items,
        Long nextCursor,
        boolean hasNext
) {
    public record FoodItemDTO(
            Long foodId,
            String name,
            String imageUrl,
            String allergyCodes,
            Integer calories,
            BigDecimal carbs,
            BigDecimal protein,
            BigDecimal fat
    ) {}
}