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

/**
 * AI 식단 생성에 사용할 "허용 음식 목록(allowlist)"을 구성하는 서비스.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>활성 음식({@code isActive=true})을 조회하여 AI 입력으로 제공할 DTO 목록을 만든다.</li>
 *   <li>알레르기 코드 정보를 반영하여 허용 가능한 음식만 남긴다.</li>
 *   <li>DTO 목록을 compact JSON 문자열로 직렬화하여 프롬프트에 삽입 가능하게 한다.</li>
 * </ul>
 *
 * <p><b>알레르기 필터 정책</b></p>
 * <ul>
 *   <li>{@code allergies}는 CSV 문자열이므로, 서버에서 단순 부분 문자열 매칭으로 제외한다.</li>
 *   <li>여러 알레르기는 "하나라도 포함되면 제외"하는 방식으로 필터링한다.</li>
 * </ul>
 *
 * <p><b>용량 제한</b></p>
 * <ul>
 *   <li>AI 입력 토큰 비용을 고려하여 최대 개수를 제한한다.</li>
 *   <li>현재 구현은 최소 10, 최대 300 범위로 clamp한다.</li>
 * </ul>
 *
 * <p><b>예외</b></p>
 * 직렬화 실패 시 {@link BusinessException}({@link ErrorCode#AI_ALLOWED_FOODS_BUILD_FAILED})를 발생시킨다.
 */
@Service
@RequiredArgsConstructor
public class DietAiFoodCatalogService {

    private final FoodRepository foodRepository;
    private final ObjectMapper objectMapper;

    /**
     * 허용 음식 목록 payload를 생성한다.
     *
     * <p>
     * 반환값에는 DTO 리스트와 동일 데이터의 JSON 문자열이 함께 포함된다.
     * JSON은 pretty print 없이 compact 형태로 생성한다.
     * </p>
     *
     * @param allergies 알레르기 코드 목록(선택)
     * @param maxFoods  요청 최대치(입력). 내부 정책에 따라 10~300 사이로 조정된다.
     * @return 허용 음식 목록 payload
     * @throws BusinessException JSON 직렬화 실패 시
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

    /**
     * 허용 음식 목록 payload.
     *
     * @param allowedFoods     허용 음식 DTO 리스트
     * @param allowedFoodsJson 동일 데이터의 JSON 배열 문자열(프롬프트 삽입용)
     */
    public record AllowedFoodsPayload(
            List<AllowedFoodDTO> allowedFoods,
            String allowedFoodsJson
    ) {}
}
