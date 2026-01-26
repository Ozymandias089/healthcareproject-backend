package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.ReportCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.ReportCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.ReportService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 신고 접수
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ApiResponse<ReportCreateResponseDTO> createReport(
            @CurrentUserId Long userId,
            @Valid @RequestBody ReportCreateRequestDTO request
    ) {
        return ApiResponse.created(reportService.createReport(userId, request));
    }
}