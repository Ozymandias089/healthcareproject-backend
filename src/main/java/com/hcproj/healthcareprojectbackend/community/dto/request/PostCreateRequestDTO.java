// ▼ [수정] 이 줄이 틀려있을 확률 100%입니다. 꼭 덮어쓰세요!
package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequestDTO(
        @NotBlank String category,
        @NotBlank String title,
        @NotBlank String content,
        Boolean isNotice
) {}