package com.hcproj.healthcareprojectbackend.upload.dto.response;

import lombok.Builder;

/**
 * Presigned URL 응답 DTO
 */
@Builder
public record PresignedUrlResponseDTO(
        String presignedUrl,   // Presigned PUT URL (이 URL로 파일 업로드)
        String fileUrl,        // 업로드 완료 후 접근 가능한 파일 URL
        String fileKey         // S3 파일 키
) {}