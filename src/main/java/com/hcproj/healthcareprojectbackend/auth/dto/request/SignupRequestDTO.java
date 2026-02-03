package com.hcproj.healthcareprojectbackend.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequestDTO(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String nickname,
        String phoneNumber,
        String profileImageUrl
) {}
