package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import com.hcproj.healthcareprojectbackend.me.service.MeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * /api/me/settings, /api/me/onboarding 등 "마이페이지 설정/온보딩" 컨트롤러
 * - 아직 DTO/스펙 미확정이면 여기 뼈대만 잡아둠
 */
@RestController
@RequiredArgsConstructor
public class MeSettingsController {

    private final MeService meService;

    /**
     * 설정 통합조회 (스펙 확정되면 Response DTO 추가)
     * GET /api/me/settings
     */
    @GetMapping("/api/me/settings")
    public ApiResponse<Object> getSettings(@CurrentUserId Long userId) {
        // TODO: meService.getSettings(userId) 구현 후 DTO로 교체
        return ApiResponse.ok(null);
    }

    /**
     * 온보딩 통합 저장 (스펙 확정되면 Request DTO 추가)
     * PUT /api/me/onboarding
     */
    @PutMapping("/api/me/onboarding")
    public ApiResponse<MessageResponseDTO> saveOnboarding(@CurrentUserId Long userId) {
        // TODO: meService.saveOnboarding(userId, request)
        return ApiResponse.ok(new MessageResponseDTO("ONBOARDING_SAVED"));
    }
}
