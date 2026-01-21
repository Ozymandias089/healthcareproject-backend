package com.hcproj.healthcareprojectbackend.diet.service;

import com.hcproj.healthcareprojectbackend.diet.dto.response.DietDayResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.DietMealItemCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.DietMealItemEntity;
import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import com.hcproj.healthcareprojectbackend.diet.repository.DietDayRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealItemRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.DietMealRepository;
import com.hcproj.healthcareprojectbackend.diet.repository.FoodRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 식단 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DietDayService {

    private final DietDayRepository dietDayRepository;
    private final DietMealRepository dietMealRepository;
    private final DietMealItemRepository dietMealItemRepository;
    private final FoodRepository foodRepository;

    /**
     * 특정 날짜의 식단 조회
     *
     * @param userId 사용자 ID
     * @param date   조회할 날짜
     * @return 식단 응답 DTO
     */
    public DietDayResponseDTO getDietDayByDate(Long userId, LocalDate date) {
        // 1. diet_days 조회 (user_id + log_date)
        DietDayEntity dietDay = dietDayRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_DAY_NOT_FOUND));

        // 2. diet_meals 조회 (sort_order ASC)
        List<DietMealEntity> meals = dietMealRepository
                .findAllByDietDayIdOrderBySortOrderAsc(dietDay.getDietDayId());

        // 3. 모든 meal_id 추출
        List<Long> mealIds = meals.stream()
                .map(DietMealEntity::getDietMealId)
                .toList();

        // 4. diet_meal_items 일괄 조회 (N+1 방지)
        List<DietMealItemEntity> allItems = dietMealItemRepository.findAllByDietMealIdIn(mealIds);

        // 5. food_id 추출 → foods 일괄 조회
        List<Long> foodIds = allItems.stream()
                .map(DietMealItemEntity::getFoodId)
                .distinct()
                .toList();

        Map<Long, FoodEntity> foodMap = foodRepository.findAllById(foodIds)
                .stream()
                .collect(Collectors.toMap(FoodEntity::getFoodId, f -> f));

        // 6. meal_id별 items 그룹핑
        Map<Long, List<DietMealItemEntity>> itemsByMealId = allItems.stream()
                .collect(Collectors.groupingBy(DietMealItemEntity::getDietMealId));

        // 7. meals 변환
        List<DietDayResponseDTO.MealDTO> mealDTOs = meals.stream()
                .map(meal -> {
                    List<DietMealItemEntity> mealItems = itemsByMealId.getOrDefault(
                            meal.getDietMealId(), List.of()
                    );

                    List<DietDayResponseDTO.MealItemDTO> itemDTOs = mealItems.stream()
                            .map(item -> {
                                FoodEntity food = foodMap.get(item.getFoodId());
                                return DietDayResponseDTO.MealItemDTO.builder()
                                        .dietMealItemId(item.getDietMealItemId())
                                        .foodId(item.getFoodId())
                                        .name(food != null ? food.getName() : "알 수 없는 음식")
                                        .calories(food != null ? food.getCalories() : 0)
                                        .carbs(food != null ? food.getCarbs() : null)
                                        .proteins(food != null ? food.getProtein() : null)
                                        .fats(food != null ? food.getFat() : null)
                                        .build();
                            })
                            .toList();

                    return DietDayResponseDTO.MealDTO.builder()
                            .dietMealId(meal.getDietMealId())
                            .sortOrder(meal.getSortOrder())
                            .items(itemDTOs)
                            .build();
                })
                .toList();

        // 8. 응답 DTO 생성
        return DietDayResponseDTO.builder()
                .date(date.toString())
                .dietDayId(dietDay.getDietDayId())
                .meals(mealDTOs)
                .build();
    }
    /**
     * 식단 항목 체크 상태 업데이트
     *
     * @param userId         사용자 ID
     * @param dietMealItemId 식단 항목 ID
     * @param checked        체크 상태
     * @return 업데이트된 응답 DTO
     */
    @Transactional
    public DietMealItemCheckResponseDTO updateDietMealItemCheck(Long userId, Long dietMealItemId, Boolean checked) {
        // 1. diet_meal_item 조회
        DietMealItemEntity dietMealItem = dietMealItemRepository.findById(dietMealItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_MEAL_ITEM_NOT_FOUND));

        // 2. diet_meal 조회
        DietMealEntity dietMeal = dietMealRepository.findById(dietMealItem.getDietMealId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_DAY_NOT_FOUND));

        // 3. diet_day 조회하여 소유권 확인
        DietDayEntity dietDay = dietDayRepository.findById(dietMeal.getDietDayId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DIET_DAY_NOT_FOUND));

        // 4. 본인 소유인지 확인
        if (!dietDay.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 5. 체크 상태 업데이트
        dietMealItem.updateChecked(checked);

        // 6. 응답 반환
        return DietMealItemCheckResponseDTO.builder()
                .message("DIET_MEAL_ITEM_CHECKED")
                .dietMealItemId(dietMealItem.getDietMealItemId())
                .checked(dietMealItem.getIsChecked())
                .build();
    }
}