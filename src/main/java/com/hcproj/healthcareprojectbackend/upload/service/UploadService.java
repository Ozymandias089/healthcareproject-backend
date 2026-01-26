package com.hcproj.healthcareprojectbackend.upload.service;

import com.hcproj.healthcareprojectbackend.global.config.properties.S3Properties;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.upload.dto.request.PresignedUrlRequestDTO;
import com.hcproj.healthcareprojectbackend.upload.dto.response.PresignedUrlResponseDTO;
import com.hcproj.healthcareprojectbackend.upload.entity.UploadType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 파일 업로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * 최대 파일 크기 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * Presigned URL 생성
     */
    public PresignedUrlResponseDTO generatePresignedUrl(Long userId, PresignedUrlRequestDTO request) {
        /* 1. 업로드 타입 검증 */
        UploadType uploadType;
        try {
            uploadType = UploadType.fromString(request.uploadType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_UPLOAD_TYPE);
        }

        /* 2. 파일 크기 검증 */
        if (request.fileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        /* 3. 확장자 검증 */
        String extension = extractExtension(request.fileName());
        if (!uploadType.isAllowedExtension(extension)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        /* 4. S3 파일 키 생성 */
        String fileKey = generateFileKey(uploadType, userId, extension);

        /* 5. Presigned URL 생성 */
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileKey)
                    .contentType(request.contentType())
                    .contentLength(request.fileSize())
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(s3Properties.getPresignedUrlExpiration()))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            /* 6. 파일 접근 URL 생성 */
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    s3Properties.getBucket(),
                    s3Properties.getRegion(),
                    fileKey
            );

            return PresignedUrlResponseDTO.builder()
                    .presignedUrl(presignedRequest.url().toString())
                    .fileUrl(fileUrl)
                    .fileKey(fileKey)
                    .build();

        } catch (Exception e) {
            log.error("Presigned URL 생성 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * S3 파일 키 생성
     * 형식: {folder}/{userId}/{timestamp}_{uuid}.{ext}
     */
    private String generateFileKey(UploadType uploadType, Long userId, String extension) {
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s/%d/%s_%s.%s",
                uploadType.getFolder(),
                userId,
                timestamp,
                uuid,
                extension
        );
    }
}