package com.hcproj.healthcareprojectbackend.diet.ai.dto;

/**
 * AI에게 제공할 허용 음식(allowlist) DTO.
 *
 * <p>
 * AI는 이 목록의 {@code id} 값만 foodId로 사용할 수 있다.
 * </p>
 *
 * @param id              음식 ID(foodId)
 * @param name            음식 이름
 * @param calories        칼로리
 * @param nutritionUnit   영양 단위(예: G/ML/UNIT/CUP/L)
 * @param nutritionAmount 단위 기준량
 */
public record AllowedFoodDTO(
        Long id,
        String name,
        Integer calories,
        String nutritionUnit,
        Integer nutritionAmount
) {}
