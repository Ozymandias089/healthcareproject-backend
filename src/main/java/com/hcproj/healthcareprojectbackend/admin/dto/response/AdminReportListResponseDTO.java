package com.hcproj.healthcareprojectbackend.admin.dto.response;

import com.hcproj.healthcareprojectbackend.community.entity.ReportStatus;
import com.hcproj.healthcareprojectbackend.community.entity.ReportType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AdminReportListResponseDTO(
        long total,
        List<AdminReportItemDTO> list
) {
    @Builder
    public record AdminReportItemDTO(
            Long reportId,
            String reporterHandle,
            ReportType type,
            Long targetId,
            String reason,
            ReportStatus status,
            Instant createdAt
    ) {}
}