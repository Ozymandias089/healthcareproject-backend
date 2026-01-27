package com.hcproj.healthcareprojectbackend.diet.ai;

import com.hcproj.healthcareprojectbackend.diet.ai.dto.DietAiWeekPlanResult;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * AI가 생성한 주간 식단 결과의 정합성을 검증하는 컴포넌트.
 *
 * <p><b>검증 목적</b></p>
 * <ul>
 *   <li>AI 출력이 스키마를 준수하고, 비즈니스 제약(7일/연속 displayOrder/양수 count)을 만족하는지 확인한다.</li>
 *   <li>allowlist에 없는 foodId가 포함되었는지 검출하여 차단한다.</li>
 * </ul>
 *
 * <p><b>검증 항목</b></p>
 * <ul>
 *   <li>startDate/endDate가 요청 범위(startDate..startDate+6)와 일치</li>
 *   <li>days가 정확히 7개이며 날짜가 중복/누락 없이 범위를 정확히 포함</li>
 *   <li>각 day는 meals가 비어있지 않음</li>
 *   <li>meal.displayOrder는 0부터 연속</li>
 *   <li>meal.title은 공백이 아님</li>
 *   <li>각 item은 allowlist에 포함된 foodId이며 count는 양수</li>
 * </ul>
 *
 * <p><b>실패 처리</b></p>
 * 검증 실패 시 {@link BusinessException}({@link ErrorCode#AI_INVALID_OUTPUT})를 발생시킨다.
 */
@Component
public class DietAiOutputValidator {

    /**
     * 주간 식단 결과를 검증한다.
     *
     * @param startDate      요청한 주간 시작일
     * @param result         AI 생성 결과
     * @param allowedFoodIds 허용된 foodId 집합(allowlist)
     * @throws BusinessException 검증 실패 시
     */
    public void validate(LocalDate startDate, DietAiWeekPlanResult result, Set<Long> allowedFoodIds) {
        if (result == null) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

        LocalDate expectedEnd = startDate.plusDays(6);
        if (!startDate.equals(result.startDate()) || !expectedEnd.equals(result.endDate())) {
            throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
        }

        if (result.days() == null || result.days().size() != 7) {
            throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
        }

        Set<LocalDate> daySet = new HashSet<>();
        for (var day : result.days()) {
            if (day.logDate() == null) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
            daySet.add(day.logDate());

            if (day.meals() == null || day.meals().isEmpty()) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

            int expectedOrder = 0;
            for (var meal : day.meals()) {
                if (meal.displayOrder() == null || meal.displayOrder() != expectedOrder) {
                    throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
                }
                expectedOrder++;

                if (meal.title() == null || meal.title().isBlank()) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
                if (meal.items() == null || meal.items().isEmpty()) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);

                for (var item : meal.items()) {
                    if (item.foodId() == null || !allowedFoodIds.contains(item.foodId())) {
                        throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
                    }
                    if (item.count() == null || item.count() <= 0) {
                        throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
                    }
                }
            }
        }

        // 날짜가 startDate..startDate+6을 정확히 포함하는지
        for (int i = 0; i < 7; i++) {
            if (!daySet.contains(startDate.plusDays(i))) throw new BusinessException(ErrorCode.AI_INVALID_OUTPUT);
        }
    }
}
