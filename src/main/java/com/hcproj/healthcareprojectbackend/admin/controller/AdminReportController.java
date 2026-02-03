package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.ReportStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.Admincommentdetailresponsedto;
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

    // 신고 목록 조회 (상태별 + 타입별 필터링)
    // 예: GET /api/admin/reports?type=POST (게시글 신고만 보기)
    @AdminOnly
    @GetMapping
    public ApiResponse<AdminReportListResponseDTO> getReportList(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "type", required = false) String type
    ) {
        return ApiResponse.ok(adminReportService.getReportList(status, type));
    }

    // 신고 처리 (통합 API)
    @AdminOnly
    @PatchMapping("/{reportId}/status")
    public ApiResponse<MessageResponseDTO> updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportStatusUpdateRequestDTO request
    ) {
        adminReportService.updateReportStatus(reportId, request);
        return ApiResponse.ok(new MessageResponseDTO("REPORT_STATUS_UPDATED"));
    }

    // 댓글 상세 조회 (신고된 댓글 확인용)
    @AdminOnly
    @GetMapping("/comments/{commentId}")
    public ApiResponse<Admincommentdetailresponsedto> getCommentDetail(
            @PathVariable Long commentId
    ) {
        return ApiResponse.ok(adminReportService.getCommentDetail(commentId));
    }
}