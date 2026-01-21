package com.hcproj.healthcareprojectbackend.diet.controller;

import com.hcproj.healthcareprojectbackend.diet.dto.request.DietMealItemCheckRequestDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.DietMealItemCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.service.DietDayService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 식단 항목 관련 API 컨트롤러
 * - 엔드포인트: /api/diet-meal-items/**
 */
@RestController
@RequestMapping("/api/diet-meal-items")
@RequiredArgsConstructor
public class DietMealItemController {

    private final DietDayService dietDayService;

    /**
     * 식단 항목 체크 상태 업데이트
     *
     * @param userId         현재 로그인한 사용자 ID (JWT에서 추출)
     * @param dietMealItemId 식단 항목 ID
     * @param request        체크 상태 요청
     * @return 업데이트 결과
     */
    @PatchMapping("/{dietMealItemId}/check")
    public ResponseEntity<ApiResponse<DietMealItemCheckResponseDTO>> updateDietMealItemCheck(
            @CurrentUserId Long userId,
            @PathVariable Long dietMealItemId,
            @RequestBody DietMealItemCheckRequestDTO request
    ) {
        DietMealItemCheckResponseDTO response = dietDayService.updateDietMealItemCheck(
                userId, dietMealItemId, request.checked()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}