package com.hcproj.healthcareprojectbackend.diet.ai.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * AI 주간 식단 생성 요청 DTO.
 *
 * <p>
 * 알레르기 정보와 추가 요청(note)을 전달한다.
 * note는 최대 500자까지 허용한다.
 * </p>
 *
 * @param allergies 알레르기 코드 목록(선택)
 * @param note      추가 요청/주의사항(선택, 최대 500자)
 */
public record AiDietWeekPlanPutRequestDTO(
        List<String> allergies,
        @Size(max = 500)
        String note
) {}