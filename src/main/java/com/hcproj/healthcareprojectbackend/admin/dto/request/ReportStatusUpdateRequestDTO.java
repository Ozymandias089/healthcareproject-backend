package com.hcproj.healthcareprojectbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportStatusUpdateRequestDTO(
        @NotBlank(message = "변경할 상태(PROCESSED/REJECTED)는 필수입니다.")
        String status
) {}