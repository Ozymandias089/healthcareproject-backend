package com.hcproj.healthcareprojectbackend.workout.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.service.ExerciseService;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseListResponseDTO;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.workout.dto.request.ExerciseCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseCreateResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;

    /**
     * 운동 리스트 조회 API (무한 스크롤 + 검색 + 필터).
     */
    @GetMapping
    public ApiResponse<ExerciseListResponseDTO> getExerciseList(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "bodyPart") List<String> bodyParts,
            @RequestParam(required = false, name = "difficulty") List<String> difficulties
    ) {
        ExerciseListResponseDTO response = exerciseService.getExerciseList(cursor, limit, keyword, bodyParts, difficulties);
        return ApiResponse.ok(response);
    }

    // 운동 상세 조회
    @GetMapping("/{exerciseId}")
    public ApiResponse<ExerciseDetailResponseDTO> getExerciseDetail(
            @PathVariable Long exerciseId
    ) {
        ExerciseDetailResponseDTO response = exerciseService.getExerciseDetail(exerciseId);
        return ApiResponse.ok(response);
    }
    /**
     * 운동 등록 API (관리자 전용)
     * POST /api/exercises
     */
    @AdminOnly
    @PostMapping
    public ApiResponse<ExerciseCreateResponseDTO> createExercise(
            @Valid @RequestBody ExerciseCreateRequestDTO request
    ) {
        return ApiResponse.created(exerciseService.createExercise(request));
    }
    /**
     * 운동 삭제 API (관리자 전용)
     * DELETE /api/exercises/{exerciseId}
     */
    @AdminOnly
    @DeleteMapping("/{exerciseId}")
    public ApiResponse<ExerciseCreateResponseDTO> deleteExercise(
            @PathVariable Long exerciseId
    ) {
        return ApiResponse.ok(exerciseService.deleteExercise(exerciseId));
    }
}