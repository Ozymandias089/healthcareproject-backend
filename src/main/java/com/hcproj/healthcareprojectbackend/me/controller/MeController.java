package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.request.WithdrawalRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MeResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import com.hcproj.healthcareprojectbackend.me.service.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * /api/me (내 계정) 루트 컨트롤러
 * - 내 기본정보 조회
 * - 회원탈퇴
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MeController {

    private final MeService meService;

    /**
     * 내 기본정보 조회
     * GET /api/me
     */
    @GetMapping(produces = "application/json")
    public ApiResponse<MeResponseDTO> me(@CurrentUserId Long userId) {
        return ApiResponse.ok(meService.getMe(userId));
    }

    /**
     * 회원탈퇴
     * DELETE /api/me
     */
    @DeleteMapping(consumes = "application/json", produces = "application/json")
    public ApiResponse<MessageResponseDTO> withdraw(
            @CurrentUserId Long userId,
            @Valid @RequestBody WithdrawalRequestDTO request
    ) {
        meService.withdraw(userId, request);
        return ApiResponse.ok(new MessageResponseDTO("WITHDRAWAL_SUCCESS"));
    }
}
