package com.hcproj.healthcareprojectbackend.diet.service;

import com.hcproj.healthcareprojectbackend.diet.ai.DietAiFoodCatalogService;
import com.hcproj.healthcareprojectbackend.diet.ai.DietAiOutputValidator;
import com.hcproj.healthcareprojectbackend.diet.ai.DietAiPrompts;
import com.hcproj.healthcareprojectbackend.diet.ai.dto.AiDietWeekPlanPutRequestDTO;
import com.hcproj.healthcareprojectbackend.diet.ai.dto.AllowedFoodDTO;
import com.hcproj.healthcareprojectbackend.diet.ai.dto.DietAiWeekPlanResult;
import com.hcproj.healthcareprojectbackend.diet.dto.response.AiDietWeekPlanResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.entity.*;
import com.hcproj.healthcareprojectbackend.diet.repository.*;
import com.hcproj.healthcareprojectbackend.global.ai.AiJsonCaller;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietAiWeekPlanFacade {

    private final AiJsonCaller aiJsonCaller;

    private final DietDayRepository dietDayRepository;
    private final DietMealRepository dietMealRepository;
    private final DietMealItemRepository dietMealItemRepository;
    private final FoodRepository foodRepository;

    private final DietAiFoodCatalogService foodCatalogService;
    private final DietAiOutputValidator outputValidator;

    @Transactional
    public AiDietWeekPlanResponseDTO replaceWeekPlan(Long userId, AiDietWeekPlanPutRequestDTO req) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(6);

        // 1) 기존 데이터 삭제 (range)
        deleteExistingRange(userId, startDate, endDate);

        // 2) allowedFoods 생성 (화이트리스트)
        var payload = foodCatalogService.buildAllowedFoodsPayload(req.allergies(), 120);
        if (payload.allowedFoods().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_ALLOWED_FOODS_BUILD_FAILED);
        }
        Set<Long> allowedFoodIds = payload.allowedFoods().stream().map(AllowedFoodDTO::id).collect(Collectors.toSet());

        // 3) AI 호출 (foodId 기반 JSON)
        DietAiWeekPlanResult aiResult = aiJsonCaller.callJson(
                DietAiPrompts.SYSTEM,
                DietAiPrompts.user(startDate, req.allergies(), req.note(), payload.allowedFoodsJson()),
                DietAiWeekPlanResult.class
        );

        // 4) 서버 검증 (foodId 화이트리스트 + 날짜/정렬 규칙)
        outputValidator.validate(startDate, aiResult, allowedFoodIds);

        // 5) insert
        Persisted persisted = persistAiResult(userId, aiResult);

        // 6) 응답 조립 (foods 조인 + nutrition 계산)
        return buildResponse(startDate, endDate, aiResult.consideration(), persisted);
    }

    private void deleteExistingRange(Long userId, LocalDate start, LocalDate end) {
        List<DietDayEntity> existingDays = dietDayRepository.findAllByUserIdAndLogDateBetween(userId, start, end);
        if (existingDays.isEmpty()) return;

        List<Long> dayIds = existingDays.stream().map(DietDayEntity::getDietDayId).toList();

        List<DietMealEntity> meals = dietMealRepository.findByDietDayIdIn(dayIds);
        if (!meals.isEmpty()) {
            List<Long> mealIds = meals.stream().map(DietMealEntity::getDietMealId).toList();
            dietMealItemRepository.deleteByDietMealIdIn(mealIds);
            dietMealRepository.deleteByDietDayIdIn(dayIds);
        }

        dietDayRepository.deleteAll(existingDays);
    }

    private Persisted persistAiResult(Long userId, DietAiWeekPlanResult ai) {
        // day insert
        List<DietDayEntity> dayEntities = ai.days().stream()
                .sorted(Comparator.comparing(DietAiWeekPlanResult.Day::logDate))
                .map(d -> DietDayEntity.builder()
                        .userId(userId)
                        .logDate(d.logDate())
                        .build())
                .toList();

        List<DietDayEntity> savedDays = dietDayRepository.saveAll(dayEntities);
        Map<LocalDate, Long> dateToDayId = savedDays.stream()
                .collect(Collectors.toMap(DietDayEntity::getLogDate, DietDayEntity::getDietDayId));

        // meal insert
        List<DietMealEntity> mealEntities = new ArrayList<>();
        for (var day : ai.days()) {
            Long dayId = dateToDayId.get(day.logDate());
            for (var meal : day.meals()) {
                mealEntities.add(DietMealEntity.builder()
                        .dietDayId(dayId)
                        .sortOrder(meal.displayOrder())
                        .title(meal.title())
                        .build());
            }
        }
        List<DietMealEntity> savedMeals = dietMealRepository.saveAll(mealEntities);

        // (logDate, displayOrder) -> dietMealId 매핑
        // mealEntities와 savedMeals는 같은 순서로 반환되는 게 일반적이지만, DB/구현체에 의존하지 않게 안전하게 매핑하려면 재조회가 더 확실.
        // 여기선 성능 위해 "같은 순서" 전제로 간단히 처리:
        if (savedMeals.size() != mealEntities.size()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // item insert
        List<DietMealItemEntity> itemEntities = new ArrayList<>();
        int mealIdx = 0;
        for (var day : ai.days()) {
            for (var meal : day.meals()) {
                Long savedMealId = savedMeals.get(mealIdx++).getDietMealId();
                for (var item : meal.items()) {
                    itemEntities.add(DietMealItemEntity.builder()
                            .dietMealId(savedMealId)
                            .foodId(item.foodId())
                            .count(item.count())
                            .isChecked(false) // 정책 A
                            .build());
                }
            }
        }
        List<DietMealItemEntity> savedItems = dietMealItemRepository.saveAll(itemEntities);

        return new Persisted(savedDays, savedMeals, savedItems);
    }

    private AiDietWeekPlanResponseDTO buildResponse(LocalDate startDate, LocalDate endDate, String consideration, Persisted persisted) {

        // food map
        Set<Long> usedFoodIds = persisted.items.stream().map(DietMealItemEntity::getFoodId).collect(Collectors.toSet());
        Map<Long, FoodEntity> foodMap = foodRepository.findByFoodIdIn(usedFoodIds).stream()
                .collect(Collectors.toMap(FoodEntity::getFoodId, f -> f));

        // dayId -> meals, mealId -> items
        Map<Long, List<DietMealEntity>> mealsByDayId = persisted.meals.stream()
                .collect(Collectors.groupingBy(DietMealEntity::getDietDayId));

        Map<Long, List<DietMealItemEntity>> itemsByMealId = persisted.items.stream()
                .collect(Collectors.groupingBy(DietMealItemEntity::getDietMealId));

        List<AiDietWeekPlanResponseDTO.Day> days = persisted.days.stream()
                .sorted(Comparator.comparing(DietDayEntity::getLogDate))
                .map(day -> {
                    List<DietMealEntity> dayMeals = mealsByDayId.getOrDefault(day.getDietDayId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing(DietMealEntity::getSortOrder))
                            .toList();

                    List<AiDietWeekPlanResponseDTO.Meal> mealDtos = new ArrayList<>();
                    int dayTotalCalories = 0;

                    for (DietMealEntity meal : dayMeals) {
                        List<DietMealItemEntity> mealItems = itemsByMealId.getOrDefault(meal.getDietMealId(), List.of());

                        // items dto + nutrition sum
                        int mealCalories = 0;
                        BigDecimal mealCarbs = BigDecimal.ZERO;
                        BigDecimal mealProtein = BigDecimal.ZERO;
                        BigDecimal mealFat = BigDecimal.ZERO;

                        boolean mealChecked = !mealItems.isEmpty() && mealItems.stream().allMatch(i -> Boolean.TRUE.equals(i.getIsChecked()));

                        List<AiDietWeekPlanResponseDTO.Item> itemDtos = new ArrayList<>();
                        for (DietMealItemEntity it : mealItems) {
                            FoodEntity food = foodMap.get(it.getFoodId());
                            if (food == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

                            int count = it.getCount() == null ? 0 : it.getCount();

                            // calories/carbs/protein/fat는 Food 기준(1 serving) * count
                            mealCalories += safeInt(food.getCalories()) * count;
                            mealCarbs = mealCarbs.add(safeBd(food.getCarbs()).multiply(BigDecimal.valueOf(count)));
                            mealProtein = mealProtein.add(safeBd(food.getProtein()).multiply(BigDecimal.valueOf(count)));
                            mealFat = mealFat.add(safeBd(food.getFat()).multiply(BigDecimal.valueOf(count)));

                            itemDtos.add(new AiDietWeekPlanResponseDTO.Item(
                                    it.getDietMealItemId(),
                                    food.getFoodId(),
                                    food.getName(),
                                    food.getImageUrl(),
                                    food.getNutritionUnit(),
                                    food.getNutritionAmount(),
                                    null, // grams: 현재 모델 없음
                                    it.getCount(),
                                    it.getIsChecked()
                            ));
                        }

                        dayTotalCalories += mealCalories;

                        mealDtos.add(new AiDietWeekPlanResponseDTO.Meal(
                                meal.getDietMealId(),
                                meal.getSortOrder(),
                                mealChecked,
                                meal.getTitle(),
                                new AiDietWeekPlanResponseDTO.Nutrition(
                                        mealCalories,
                                        scale2(mealCarbs),
                                        scale2(mealProtein),
                                        scale2(mealFat)
                                ),
                                itemDtos
                        ));
                    }

                    AiDietWeekPlanResponseDTO.Summary summary =
                            new AiDietWeekPlanResponseDTO.Summary(dayTotalCalories, dayMeals.size());

                    return new AiDietWeekPlanResponseDTO.Day(
                            day.getDietDayId(),
                            day.getLogDate(),
                            summary,
                            mealDtos
                    );
                })
                .toList();

        return new AiDietWeekPlanResponseDTO(
                startDate,
                endDate,
                consideration,
                days,
                new AiDietWeekPlanResponseDTO.PageInfo(7)
        );
    }

    private int safeInt(Integer v) { return v == null ? 0 : v; }
    private BigDecimal safeBd(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal scale2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private record Persisted(
            List<DietDayEntity> days,
            List<DietMealEntity> meals,
            List<DietMealItemEntity> items
    ) {}
}
