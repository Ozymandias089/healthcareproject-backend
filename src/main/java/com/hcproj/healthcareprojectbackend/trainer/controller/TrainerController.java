package com.hcproj.healthcareprojectbackend.trainer.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.trainer.dto.request.TrainerApplicationRequestDTO;
import com.hcproj.healthcareprojectbackend.trainer.dto.response.TrainerApplicationResponseDTO;
import com.hcproj.healthcareprojectbackend.trainer.service.TrainerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trainer")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    // [USER-009] 트레이너 증빙자료 제출
    @PostMapping("/application")
    public ApiResponse<TrainerApplicationResponseDTO> submitApplication(
            @CurrentUserId Long userId,
            @Valid @RequestBody TrainerApplicationRequestDTO request
    ) {
        return ApiResponse.created(trainerService.submitApplication(userId, request));
    }
}