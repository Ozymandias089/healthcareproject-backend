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
            String reporterHandle,      // 신고자
            String targetAuthorHandle,  // 신고 당한 사람
            ReportType type,
            Long targetId,
            String reason,
            ReportStatus status,
            Instant createdAt
    ) {}
}