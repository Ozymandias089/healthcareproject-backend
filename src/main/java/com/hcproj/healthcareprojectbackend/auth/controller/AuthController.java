package com.hcproj.healthcareprojectbackend.auth.controller;

import com.hcproj.healthcareprojectbackend.auth.dto.request.EmailCheckRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.LoginRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.SignupRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.request.TokenReissueRequestDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.TokenResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.service.AuthService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<TokenResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        return ApiResponse.ok(authService.signup(request));
    }

    // 이메일 중복 체크
    @PostMapping("/email/check")
    public ApiResponse<Void> checkEmail(@Valid @RequestBody EmailCheckRequestDTO request) {
        authService.checkEmailDuplicate(request);
        return ApiResponse.ok();
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponse.ok(authService.login(request));
    }

    // 토큰 재발급
    @PostMapping("/token/reissue")
    public ApiResponse<TokenResponseDTO> reissue(@Valid @RequestBody TokenReissueRequestDTO request) {
        return ApiResponse.ok(authService.reissue(request));
    }

    // 로그아웃 (MVP: refreshToken 무효화 전략은 팀 합의 필요)
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(name = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok();
    }

    // 소셜 로그인 (스펙 확정 전이라 껍데기만)
    @PostMapping("/social/login")
    public ApiResponse<TokenResponseDTO> socialLogin() {
        // TODO: provider, code/token payload 확정되면 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // 패스워드 재설정 메일 발송
    @PostMapping("/password/reset/request")
    public ApiResponse<Void> requestPasswordReset() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // 패스워드 재설정
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
