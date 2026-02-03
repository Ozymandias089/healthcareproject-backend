package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequestDTO(
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content, // Toast UI 마크다운
        Boolean isNotice
) {}