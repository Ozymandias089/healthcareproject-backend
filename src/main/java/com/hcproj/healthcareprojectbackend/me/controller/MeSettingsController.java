package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.request.ChangeNicknameRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.request.ChangePhoneNumberRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.request.ChangeProfileImageRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.request.OnboardingRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MeResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import com.hcproj.healthcareprojectbackend.me.service.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * /api/me/settings, /api/me/onboarding 등 "마이페이지 설정/온보딩" 컨트롤러
 * - 아직 DTO/스펙 미확정이면 여기 뼈대만 잡아둠
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeSettingsController {

    private final MeService meService;

    /**
     * 온보딩 통합 저장 (스펙 확정되면 Request DTO 추가)
     * PUT /api/me/onboarding
     */
    @PutMapping(path = "/onboarding", consumes = "application/json", produces = "application/json")
    public ApiResponse<MessageResponseDTO> saveOnboarding(@CurrentUserId Long userId,
                                                          @Valid @RequestBody OnboardingRequestDTO request) {
        meService.onboarding(userId, request);
        return ApiResponse.ok(new MessageResponseDTO("ONBOARDING_SAVED"));
    }

    @PatchMapping(path = "/nickname", consumes = "application/json", produces = "application/json")
    public ApiResponse<MeResponseDTO> changeNickname(
            @CurrentUserId Long userId,
            @Valid @RequestBody ChangeNicknameRequestDTO request
    ) {
        MeResponseDTO response = meService.changeNickname(userId, request);
        return ApiResponse.ok(response);
    }

    @PatchMapping(path = "/phone", consumes = "application/json", produces = "application/json")
    public ApiResponse<MeResponseDTO> changePhone(
            @CurrentUserId Long userId,
            @Valid @RequestBody ChangePhoneNumberRequestDTO request
    ) {
        return ApiResponse.ok(meService.changePhoneNumber(userId, request));
    }

    @PatchMapping(path = "/profile-image", consumes = "application/json", produces = "application/json")
    public ApiResponse<MeResponseDTO> changeProfileImage(
            @CurrentUserId Long userId,
            @Valid @RequestBody ChangeProfileImageRequestDTO request
    ) {
        return ApiResponse.ok(meService.changeProfileImageUrl(userId, request));
    }

    @GetMapping(path = "/onboarding/status")
    public ApiResponse<Boolean> onboardingStatus(@CurrentUserId Long userId) {
        return ApiResponse.ok(meService.onboardingStatus(userId));
    }
}
