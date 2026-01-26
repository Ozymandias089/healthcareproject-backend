package com.hcproj.healthcareprojectbackend.diet.ai;

import com.hcproj.healthcareprojectbackend.diet.ai.dto.DietAiWeekPlanResult;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DietAiOutputValidator {

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
