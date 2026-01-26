package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record ReportCreateResponseDTO(
        String message,
        Instant createdAt
) {
    public static ReportCreateResponseDTO of(Instant createdAt) {
        return new ReportCreateResponseDTO("REPORT_CREATED", createdAt);
    }
}