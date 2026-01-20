package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequestDTO(
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content,
        Boolean isNotice
) {}
