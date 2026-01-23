package com.hcproj.healthcareprojectbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminNoticeCreateRequestDTO(
        @NotBlank(message = "카테고리는 필수입니다.")
        String category,

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotNull(message = "공지 여부는 필수입니다.")
        Boolean isNotice
) {}