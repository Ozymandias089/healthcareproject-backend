package com.hcproj.healthcareprojectbackend.workout.ai.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 운동 루틴 생성 AI의 응답(JSON)을 매핑하기 위한 DTO.
 *
 * <p><b>매핑 대상</b></p>
 * WorkoutAiPrompts에 정의된 JSON Schema와 1:1로 대응한다.
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>AI 출력이 스키마를 위반하거나 필드명이 변경되면 파싱에 실패할 수 있다.</li>
 *   <li>exerciseId는 allowlist에 포함된 값만 허용된다(프롬프트로 강제).</li>
 * </ul>
 *
 * @param considerations 루틴 생성 시 고려 사항(자유 텍스트)
 * @param days           날짜별 루틴
 */
public record WorkoutAiRoutineResult(
        List<String> considerations,
        List<Day> days
) {

    /**
     * 특정 날짜의 루틴.
     *
     * @param logDate      루틴 날짜
     * @param title        루틴 제목
     * @param totalMinutes 총 예상 시간(분)
     * @param items        운동 항목 목록
     */
    public record Day(
            LocalDate logDate,
            String title,
            Integer totalMinutes,
            List<Item> items
    ) {}

    /**
     * 개별 운동 항목.
     *
     * <p>
     * 세트/횟수 기반 또는 시간/거리 기반 운동을 모두 표현할 수 있도록 일부 필드는 nullable이다.
     * </p>
     *
     * @param displayOrder 하루 내 표시 순서(0부터 연속)
     * @param exerciseId   참조 운동 ID (허용 목록 내 값만)
     * @param sets         세트 수(선택)
     * @param reps         반복 횟수(선택)
     * @param restSecond   휴식(초)(선택)
     * @param durationMinutes 유산소/시간 기반(분)(선택)
     * @param distanceKm   거리 기반(km)(선택)
     * @param rpe          자각강도(선택)
     * @param amount       자유 입력(예: 중량/설명)(선택)
     */
    public record Item(
            Integer displayOrder,
            Long exerciseId,
            Integer sets,
            Integer reps,
            Integer restSecond,
            Integer durationMinutes,
            java.math.BigDecimal distanceKm,
            Integer rpe,
            String amount
    ) {}
}
