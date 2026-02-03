package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequestDTO(
        @NotBlank String content
) {}
