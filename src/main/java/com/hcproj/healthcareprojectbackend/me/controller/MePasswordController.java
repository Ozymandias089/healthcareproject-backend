package com.hcproj.healthcareprojectbackend.me.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.request.PasswordChangeRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import com.hcproj.healthcareprojectbackend.me.service.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * /api/me/password 관련 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MePasswordController {

    private final MeService meService;

    /**
     * 비밀번호 변경(로그인 상태)
     * PATCH /api/me/password
     */
    @PatchMapping(path = "/password", consumes = "application/json", produces = "application/json")
    public ApiResponse<MessageResponseDTO> changePassword(
            @CurrentUserId Long userId,
            @Valid @RequestBody PasswordChangeRequestDTO request
    ) {
        meService.changePassword(userId, request);
        return ApiResponse.ok(new MessageResponseDTO("PASSWORD_CHANGED"));
    }
}
