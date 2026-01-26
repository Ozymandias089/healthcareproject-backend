package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequestDTO(
        @NotBlank(message = "신고자 핸들은 필수입니다.")
        String reporterHandle,

        @NotBlank(message = "신고 유형(COMMENT/POST)은 필수입니다.")
        String type, // "COMMENT" or "POST"

        @NotNull(message = "신고 대상 ID는 필수입니다.")
        Long id,

        @NotBlank(message = "신고 사유는 필수입니다.")
        String cause
) {}