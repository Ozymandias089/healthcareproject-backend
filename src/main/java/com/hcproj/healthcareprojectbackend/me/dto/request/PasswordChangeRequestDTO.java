package com.hcproj.healthcareprojectbackend.me.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 상태 비밀번호 변경 요청 DTO.
 */
public record PasswordChangeRequestDTO(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {}
