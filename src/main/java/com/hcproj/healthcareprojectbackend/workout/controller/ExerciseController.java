package com.hcproj.healthcareprojectbackend.workout.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.workout.dto.response.ExerciseDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.workout.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;


    // 운동 상세 조회
    @GetMapping("/{exerciseId}")
    public ApiResponse<ExerciseDetailResponseDTO> getExerciseDetail(
            @PathVariable Long exerciseId
    ) {
        ExerciseDetailResponseDTO response = exerciseService.getExerciseDetail(exerciseId);
        return ApiResponse.ok(response);
    }
}