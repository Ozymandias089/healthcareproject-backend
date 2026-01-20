package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.response.UserAllergiesResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.UserInjuriesResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.UserProfileResponseDTO;
import com.hcproj.healthcareprojectbackend.me.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * /api/me/profile, /api/me/injuries, /api/me/allergies 등
 * 프로필 도메인 조회를 "me 라우트"로 모으기 위한 컨트롤러.
 * <p>
 * 내부 구현은 profile 모듈(service/repository)을 호출하는 형태가 될 가능성이 큼.
 */
@RestController
@RequiredArgsConstructor
public class MeProfileController {

    private final ProfileService profileService;

    /**
     * 신체정보 조회
     * GET /api/me/profile
     */
    @GetMapping("/api/me/profile")
    public ApiResponse<UserProfileResponseDTO> getProfile(@CurrentUserId Long userId) {
        UserProfileResponseDTO responseDTO = profileService.getProfile(userId);
        return ApiResponse.ok(responseDTO);
    }

    /**
     * 부상정보 조회
     * GET /api/me/injuries
     */
    @GetMapping("/api/me/injuries")
    public ApiResponse<UserInjuriesResponseDTO> getInjuries(@CurrentUserId Long userId) {
        UserInjuriesResponseDTO responseDTO = profileService.getInjuries(userId);
        return ApiResponse.ok(responseDTO);
    }

    /**
     * 알레르기 조회
     * GET /api/me/allergies
     */
    @GetMapping("/api/me/allergies")
    public ApiResponse<UserAllergiesResponseDTO> getAllergies(@CurrentUserId Long userId) {
        UserAllergiesResponseDTO responseDTO = profileService.getAllergies(userId);
        return ApiResponse.ok(responseDTO);
    }
}
