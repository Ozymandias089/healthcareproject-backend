package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.workout.dto.response.WorkoutDayResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.request.WorkoutItemCheckRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.WorkoutItemCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.service.WorkoutDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 내 운동 계획 관련 API 컨트롤러
 * - 엔드포인트: /api/me/workouts/**
 */
@RestController
@RequestMapping("/api/me/workouts")
@RequiredArgsConstructor
public class MeWorkoutController {

    private final WorkoutDayService workoutDayService;

    /**
     * 특정 날짜의 운동 계획 조회
     *
     * @param userId 현재 로그인한 사용자 ID (JWT에서 추출)
     * @param date   조회할 날짜 (YYYY-MM-DD)
     * @return 운동 계획 응답
     */
    @GetMapping("/days/{date}")
    public ResponseEntity<ApiResponse<WorkoutDayResponseDTO>> getWorkoutDayByDate(
            @CurrentUserId Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WorkoutDayResponseDTO response = workoutDayService.getWorkoutDayByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    /**
     * 운동 항목 체크 상태 업데이트
     *
     * @param userId        현재 로그인한 사용자 ID (JWT에서 추출)
     * @param workoutItemId 운동 항목 ID
     * @param request       체크 상태 요청
     * @return 업데이트 결과
     */
    @PatchMapping("/workout-items/{workoutItemId}/check")
    public ResponseEntity<ApiResponse<WorkoutItemCheckResponseDTO>> updateWorkoutItemCheck(
            @CurrentUserId Long userId,
            @PathVariable Long workoutItemId,
            @RequestBody WorkoutItemCheckRequestDTO request
    ) {
        WorkoutItemCheckResponseDTO response = workoutDayService.updateWorkoutItemCheck(
                userId, workoutItemId, request.checked()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}