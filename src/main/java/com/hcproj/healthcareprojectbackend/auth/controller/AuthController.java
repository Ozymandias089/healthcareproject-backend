package com.hcproj.healthcareprojectbackend.auth.controller;

import com.hcproj.healthcareprojectbackend.auth.dto.request.*;
import com.hcproj.healthcareprojectbackend.auth.dto.response.EmailCheckResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.dto.response.TokenResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.service.AuthService;
import com.hcproj.healthcareprojectbackend.auth.service.EmailVerificationService;
import com.hcproj.healthcareprojectbackend.auth.service.PasswordResetService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<TokenResponseDTO> signup(@Valid @RequestBody SignupRequestDTO request) {
        return ApiResponse.ok(authService.signup(request));
    }

    // 이메일 중복 체크
    @PostMapping("/email/check")
    public ApiResponse<EmailCheckResponseDTO> checkEmail(@Valid @RequestBody EmailCheckRequestDTO request) {
        EmailCheckResponseDTO responseDTO = authService.checkEmailDuplicate(request);
        return ApiResponse.ok(responseDTO);
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

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequestDTO request) {
        authService.logout(request);
        return ApiResponse.ok();
    }

    // 소셜 로그인 (스펙 확정 전이라 껍데기만)
    @PostMapping("/social/login")
    public ApiResponse<TokenResponseDTO> socialLogin(@Valid @RequestBody SocialLoginRequestDTO request) {
        return ApiResponse.ok(authService.socialLoginOrSignup(request));
    }

    // 소셜 연동 (로그인 필요)
    @PostMapping("/social/connect")
    public ApiResponse<Void> socialConnect(
            @CurrentUserId Long userId,
            @Valid @RequestBody SocialConnectRequestDTO request
    ) {
        authService.connectSocial(userId, request);
        return ApiResponse.ok();
    }

    // 소셜 연동해제 (로그인 필요)
    @PostMapping("/social/disconnect")
    public ApiResponse<Void> socialDisconnect(
            @CurrentUserId Long userId,
            @Valid @RequestBody SocialDisconnectRequestDTO request
    ) {
        authService.disconnectSocial(userId, request);
        return ApiResponse.ok();
    }

    // 패스워드 재설정 메일 발송
    @PostMapping("/password/reset/request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDTO request) {
        passwordResetService.requestPasswordResetMail(request.email());
        return ApiResponse.ok();
    }

    // 패스워드 재설정
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequestDTO request) {
        passwordResetService.resetPassword(request.token(),request.email(), request.password());
        return ApiResponse.ok();
    }

    // 이메일 인증 코드 발송
    @PostMapping("/email/verify/request")
    public ApiResponse<Void> requestEmailVerification(@Valid @RequestBody EmailVerificationRequestDTO request) {
        emailVerificationService.sendVerificationCode(request.email());
        return ApiResponse.ok();
    }

    // 이메일 인증 코드 확인
    @PostMapping("/email/verify/confirm")
    public ApiResponse<Void> confirmEmailVerification(@Valid @RequestBody EmailVerificationConfirmRequestDTO request) {
        emailVerificationService.confirmVerificationCode(request.email(), request.code());
        return ApiResponse.ok();
    }
}
