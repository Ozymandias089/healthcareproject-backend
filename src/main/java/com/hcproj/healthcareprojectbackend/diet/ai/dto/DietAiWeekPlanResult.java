package com.hcproj.healthcareprojectbackend.diet.ai.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 주간 식단 계획 생성 AI 응답(JSON)을 매핑하기 위한 DTO.
 *
 * <p>
 * DietAiPrompts에 정의된 스키마를 기준으로 파싱된다.
 * </p>
 *
 * @param startDate 주간 시작일
 * @param endDate   주간 종료일(startDate+6)
 * @param days      날짜별 식단(정확히 7개)
 */
public record DietAiWeekPlanResult(
        LocalDate startDate,
        LocalDate endDate,
        List<String> considerations,
        List<Day> days
) {
    /** 특정 날짜의 식단. */
    public record Day(LocalDate logDate, List<Meal> meals) {}
    /**
     * 한 끼 식단.
     *
     * @param displayOrder 하루 내 표시 순서(0부터 연속)
     * @param title        끼니 제목(예: 아침/점심/간식)
     * @param items        음식 항목 목록
     */
    public record Meal(Integer displayOrder, String title, List<Item> items) {}
    /**
     * 음식 항목.
     *
     * @param foodId 음식 ID(allowlist 내 값만)
     * @param count  섭취 개수(양의 정수)
     */
    public record Item(Long foodId, Integer count) {}
}
