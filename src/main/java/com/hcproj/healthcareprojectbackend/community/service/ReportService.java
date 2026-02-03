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
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
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

    // [추가] 화상 PT 방 조회용
    private final PtRoomRepository ptRoomRepository;

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

        // [이동됨] 3. 중복 신고 방지 (모든 타입에 적용하기 위해 위로 올림)
        if (reportRepository.existsByReporterIdAndTargetIdAndType(userId, request.id(), type)) {
            throw new BusinessException(ErrorCode.ALREADY_REPORTED);
        }

        // 4. 신고 대상 조회 및 검증
        if (type == ReportType.POST) {
            PostEntity post = postRepository.findById(request.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

            if (post.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

            if (Boolean.TRUE.equals(post.getIsNotice())) {
                throw new BusinessException(ErrorCode.NOTICE_REPORT_NOT_ALLOWED);
            }

        } else if (type == ReportType.COMMENT) {
            CommentEntity comment = commentRepository.findById(request.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

            if (comment.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }

        } else if (type == ReportType.PT_ROOM) { // [추가됨] 화상 PT 신고 로직
            com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity room = ptRoomRepository.findById(request.id())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

            // [방어] 본인이 만든 방 신고 불가
            if (room.getTrainerId().equals(userId)) {
                throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
            }
        }

        // 5. 신고 저장
        ReportEntity report = ReportEntity.builder()
                .reporterId(userId)
                .type(type)
                .targetId(request.id())
                .reason(request.cause()) // 내용 저장
                .status(ReportStatus.PENDING)
                .build();

        ReportEntity savedReport = reportRepository.save(report);

        return ReportCreateResponseDTO.of(savedReport.getCreatedAt());
    }
}