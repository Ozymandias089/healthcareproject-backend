package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.diet.dto.response.DietDayResponseDTO;
import com.hcproj.healthcareprojectbackend.diet.service.DietDayService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 내 식단 관련 API 컨트롤러
 * - 엔드포인트: /api/me/diets/**
 */
@RestController
@RequestMapping("/api/me/diets")
@RequiredArgsConstructor
public class MeDietController {

    private final DietDayService dietDayService;

    /**
     * 특정 날짜의 식단 조회
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param date   조회할 날짜 (YYYY-MM-DD)
     * @return 식단 응답
     */
    @GetMapping("/days/{date}")
    public ResponseEntity<ApiResponse<DietDayResponseDTO>> getDietDayByDate(
            @CurrentUserId Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DietDayResponseDTO response = dietDayService.getDietDayByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}