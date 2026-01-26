package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.ReportCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.ReportCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.*;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ReportCreateResponseDTO createReport(Long userId, ReportCreateRequestDTO request) {
        // 1. 신고자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 신고 유형 파싱
        ReportType type;
        try {
            type = ReportType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 3. 신고 대상 존재 여부 확인 (객체 조회 없이 ID 검증만 수행)
        if (type == ReportType.POST) {
            if (!postRepository.existsById(request.id())) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
        } else if (type == ReportType.COMMENT) {
            if (!commentRepository.existsById(request.id())) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }

        // 4. 신고 저장 (ID만 저장)
        ReportEntity report = ReportEntity.builder()
                .reporterId(userId)
                .type(type)
                .targetId(request.id())
                .reason(request.cause())
                .status(ReportStatus.PENDING)
                .build();

        ReportEntity savedReport = reportRepository.save(report);

        return ReportCreateResponseDTO.of(savedReport.getCreatedAt());
    }
}