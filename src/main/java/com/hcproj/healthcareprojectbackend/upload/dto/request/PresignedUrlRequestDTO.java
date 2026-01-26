package com.hcproj.healthcareprojectbackend.upload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Presigned URL 요청 DTO
 */
public record PresignedUrlRequestDTO(

        @NotBlank(message = "업로드 타입은 필수입니다.")
        String uploadType,

        @NotBlank(message = "파일명은 필수입니다.")
        String fileName,

        @NotNull(message = "파일 크기는 필수입니다.")
        @Positive(message = "파일 크기는 양수여야 합니다.")
        Long fileSize,

        @NotBlank(message = "컨텐츠 타입은 필수입니다.")
        String contentType
) {}