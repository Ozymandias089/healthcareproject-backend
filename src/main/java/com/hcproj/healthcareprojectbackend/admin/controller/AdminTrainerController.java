package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.TrainerRejectRequestDTO;
import com.hcproj.healthcareprojectbackend.trainer.dto.response.TrainerPendingListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.TrainerRejectResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminTrainerService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.trainer.dto.response.TrainerApproveResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/trainer")
@RequiredArgsConstructor
public class AdminTrainerController {

    private final AdminTrainerService adminTrainerService;

    // [ADMIN] 트레이너 가입 신청 승인
    @AdminOnly
    @PatchMapping("/{handle}/approve")
    public ApiResponse<TrainerApproveResponseDTO> approveTrainer(
            @PathVariable String handle
    ) {
        return ApiResponse.ok(adminTrainerService.approveTrainer(handle));
    }

    // [ADMIN] 트레이너 승인 대기자 목록 조회
    @AdminOnly
    @GetMapping("/pending")
    public ApiResponse<TrainerPendingListResponseDTO> getPendingTrainers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ApiResponse.ok(adminTrainerService.getPendingTrainerList(pageable));
    }
    // [추가됨] 트레이너 가입 신청 거절
    @AdminOnly
    @PatchMapping("/{handle}/reject")
    public ApiResponse<TrainerRejectResponseDTO> rejectTrainer(
            @PathVariable String handle,
            @Valid @RequestBody TrainerRejectRequestDTO request
    ) {
        return ApiResponse.ok(adminTrainerService.rejectTrainer(handle, request.reason()));
    }
}
