package com.hcproj.healthcareprojectbackend.upload.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.upload.dto.request.PresignedUrlRequestDTO;
import com.hcproj.healthcareprojectbackend.upload.dto.response.PresignedUrlResponseDTO;
import com.hcproj.healthcareprojectbackend.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 파일 업로드 컨트롤러
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * Presigned URL 발급
     * POST /api/upload/presigned-url
     */
    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponseDTO> getPresignedUrl(
            @CurrentUserId Long userId,
            @Valid @RequestBody PresignedUrlRequestDTO request
    ) {
        return ApiResponse.ok(uploadService.generatePresignedUrl(userId, request));
    }
}