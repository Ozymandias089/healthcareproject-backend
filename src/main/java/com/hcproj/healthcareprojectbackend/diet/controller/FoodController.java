package com.hcproj.healthcareprojectbackend.diet.controller;

import com.hcproj.healthcareprojectbackend.diet.dto.response.FoodDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.FoodListResponseDTO;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.diet.dto.request.FoodCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.diet.dto.response.FoodCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.service.FoodService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

/**
 * 음식(Food) 관련 API 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;
    /**
     * 음식 리스트 조회 API (무한 스크롤 + 검색).
     */
    @GetMapping
    public ApiResponse<FoodListResponseDTO> getFoodList(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String keyword
    ) {
        FoodListResponseDTO response = foodService.getFoodList(cursor, limit, keyword);
        return ApiResponse.ok(response);
    }

    /**
     * 음식 상세 조회 API.
     *
     * @param foodId 조회할 음식 ID
     * @return 음식 상세 정보
     */
    @GetMapping("/{foodId}")
    public ApiResponse<FoodDetailResponseDTO> getFoodDetail(
            @PathVariable Long foodId
    ) {
        FoodDetailResponseDTO response = foodService.getFoodDetail(foodId);
        return ApiResponse.ok(response);
    }
    /**
     * 음식 등록 API (관리자 전용)
     * POST /api/foods
     */
    @AdminOnly
    @PostMapping
    public ApiResponse<FoodCreateResponseDTO> createFood(
            @Valid @RequestBody FoodCreateRequestDTO request
    ) {
        return ApiResponse.created(foodService.createFood(request));
    }
    /**
     * 음식 삭제 API (관리자 전용)
     * DELETE /api/foods/{foodId}
     */
    @AdminOnly
    @DeleteMapping("/{foodId}")
    public ApiResponse<FoodCreateResponseDTO> deleteFood(
            @PathVariable Long foodId
    ) {
        return ApiResponse.ok(foodService.deleteFood(foodId));
    }
}