package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.ReportStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminReportListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminReportService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // 신고 목록 조회
    @AdminOnly
    @GetMapping
    public ApiResponse<AdminReportListResponseDTO> getReportList(
            @RequestParam(name = "status", required = false) String status
    ) {
        return ApiResponse.ok(adminReportService.getReportList(status));
    }

    // 신고 처리 (통합 API)
    // PROCESSED 전송 시 -> 신고 처리 + 글 삭제 + 연관 신고 일괄 처리
    // REJECTED 전송 시 -> 신고 반려 (글 유지)
    @AdminOnly
    @PatchMapping("/{reportId}/status")
    public ApiResponse<MessageResponseDTO> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportStatusUpdateRequestDTO request
    ) {
        adminReportService.updateReportStatus(reportId, request);
        return ApiResponse.ok(new MessageResponseDTO("REPORT_STATUS_UPDATED"));
    }
}