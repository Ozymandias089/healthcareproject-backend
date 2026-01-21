package com.hcproj.healthcareprojectbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminPromoteRequestDTO(
        @NotBlank
        String targetHandle // 관리자로 승격할 유저 핸들
) {}