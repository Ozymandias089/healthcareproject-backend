package com.hcproj.healthcareprojectbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrainerRejectRequestDTO(
        @NotBlank(message = "거절 사유를 입력해주세요.")
        String reason
) {}