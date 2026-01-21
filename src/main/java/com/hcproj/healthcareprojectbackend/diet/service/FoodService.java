package com.hcproj.healthcareprojectbackend.diet.service;

import com.hcproj.healthcareprojectbackend.diet.dto.response.FoodDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.FoodListResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import com.hcproj.healthcareprojectbackend.diet.repository.FoodRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 음식(Food) 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    /**
     * 음식 상세 조회.
     */
    @Transactional(readOnly = true)
    public FoodDetailResponseDTO getFoodDetail(Long foodId) {
        // 1. 음식 조회 (활성화된 음식만)
        FoodEntity food = foodRepository.findByFoodIdAndIsActiveTrue(foodId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOOD_NOT_FOUND));

        // 2. DTO 변환 후 반환
        return new FoodDetailResponseDTO(
                food.getFoodId(),
                food.getName(),
                food.getImageUrl(),
                food.getCalories(),
                food.getNutritionUnit(),
                food.getNutritionAmount(),
                food.getCarbs(),
                food.getProtein(),   // Entity는 protein, 응답은 proteins
                food.getFat()        // Entity는 fat, 응답은 fats
        );
    }
    /**
     * 음식 리스트 조회 (무한 스크롤 + 검색).
     */
    @Transactional(readOnly = true)
    public FoodListResponseDTO getFoodList(Long cursor, Integer limit, String keyword) {
        // 1. limit 유효성 검사
        int actualLimit = (limit == null || limit <= 0) ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        // 2. limit + 1개 조회 (hasNext 판단용)
        List<FoodEntity> entities = foodRepository.findFoodsWithCursor(
                cursor,
                keyword,
                actualLimit + 1
        );

        // 3. hasNext 판단
        boolean hasNext = entities.size() > actualLimit;

        // 4. 실제 반환할 데이터 (limit개만)
        List<FoodEntity> resultEntities = hasNext
                ? entities.subList(0, actualLimit)
                : entities;

        // 5. DTO 변환
        List<FoodListResponseDTO.FoodItemDTO> items = resultEntities.stream()
                .map(entity -> new FoodListResponseDTO.FoodItemDTO(
                        entity.getFoodId(),
                        entity.getName(),
                        entity.getImageUrl(),
                        entity.getAllergyCodes()
                ))
                .toList();

        // 6. nextCursor 계산
        Long nextCursor = hasNext && !resultEntities.isEmpty()
                ? resultEntities.get(resultEntities.size() - 1).getFoodId()
                : null;

        return new FoodListResponseDTO(items, nextCursor, hasNext);
    }
}