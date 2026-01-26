package com.hcproj.healthcareprojectbackend.diet.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.diet.ai.dto.AllowedFoodDTO;
import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import com.hcproj.healthcareprojectbackend.diet.repository.FoodRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietAiFoodCatalogService {

    private final FoodRepository foodRepository;
    private final ObjectMapper objectMapper;

    /**
     * AI에게 줄 allowedFoods JSON 문자열을 만든다.
     * - 토큰을 위해 최대 maxFoods개로 제한
     * - 알레르기 코드는 LIKE로 제외(전제)
     */
    public AllowedFoodsPayload buildAllowedFoodsPayload(List<String> allergies, int maxFoods) {
        int pageSize = Math.max(10, Math.min(maxFoods, 300)); // 안전 범위
        PageRequest pr = PageRequest.of(0, pageSize);

        List<FoodEntity> foods;

        if (allergies == null || allergies.isEmpty()) {
            foods = foodRepository.findByIsActiveTrue(pr);
        } else {
            // 여러 알레르기를 한 번의 쿼리로 완벽히 처리하기 까다로워서(CSV + LIKE),
            // 여기서는 "각 코드별 제외" 후 교집합 느낌으로 필터링한다.
            // (즉, allergy 중 하나라도 포함되면 제거)
            foods = foodRepository.findByIsActiveTrue(pr);
            Set<String> lowerAllergies = allergies.stream()
                    .filter(Objects::nonNull)
                    .map(a -> a.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            foods = foods.stream()
                    .filter(f -> isAllowedByAllergies(f.getAllergyCodes(), lowerAllergies))
                    .limit(pageSize)
                    .toList();
        }

        List<AllowedFoodDTO> allowed = foods.stream()
                .map(f -> new AllowedFoodDTO(
                        f.getFoodId(),
                        f.getName(),
                        f.getCalories(),
                        f.getNutritionUnit(),
                        f.getNutritionAmount()
                ))
                .toList();

        String json;
        try {
            // compact JSON (pretty print 금지)
            json = objectMapper.writeValueAsString(allowed);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.AI_ALLOWED_FOODS_BUILD_FAILED);
        }

        return new AllowedFoodsPayload(allowed, json);
    }

    private boolean isAllowedByAllergies(String allergyCodesCsv, Set<String> lowerAllergies) {
        if (allergyCodesCsv == null || allergyCodesCsv.isBlank()) return true;
        String hay = allergyCodesCsv.toLowerCase(Locale.ROOT);
        for (String code : lowerAllergies) {
            if (hay.contains(code)) return false;
        }
        return true;
    }

    public record AllowedFoodsPayload(
            List<AllowedFoodDTO> allowedFoods,
            String allowedFoodsJson
    ) {}
}
