package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.auth.dto.request.SocialConnectRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.SocialDisconnectRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.SocialConnectionsResponseDTO;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.service.MeSocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/social")
@RequiredArgsConstructor
public class MeSocialController {
    private final MeSocialService meSocialService;

    // 소셜 연동 (로그인 필요)
    @PostMapping("/connect")
    public ApiResponse<Void> socialConnect(
            @CurrentUserId Long userId,
            @Valid @RequestBody SocialConnectRequestDTO request
    ) {
        meSocialService.connectSocial(userId, request);
        return ApiResponse.ok();
    }

    // 소셜 연동해제 (로그인 필요)
    @PostMapping("/disconnect")
    public ApiResponse<Void> socialDisconnect(
            @CurrentUserId Long userId,
            @Valid @RequestBody SocialDisconnectRequestDTO request
    ) {
        meSocialService.disconnectSocial(userId, request);
        return ApiResponse.ok();
    }

    @GetMapping()
    public ApiResponse<SocialConnectionsResponseDTO> getConnectedSocial(@CurrentUserId Long userId) {
        return ApiResponse.ok(meSocialService.getSocialConnections(userId));
    }
}
