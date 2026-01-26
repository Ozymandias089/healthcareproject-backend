package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.ReportStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminReportListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.entity.*;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 1. 신고 목록 조회 (status, type 필터 적용)
    @Transactional(readOnly = true)
    public AdminReportListResponseDTO getReportList(String statusStr, String typeStr) {
        List<ReportEntity> reports;

        // (1) Status 필터링
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                ReportStatus status = ReportStatus.valueOf(statusStr.toUpperCase());
                reports = reportRepository.findAllByStatusOrderByCreatedAtDesc(status);
            } catch (IllegalArgumentException e) {
                reports = Collections.emptyList();
            }
        } else {
            reports = reportRepository.findAll();
        }

        // (2) Type(POST/COMMENT) 필터링
        if (typeStr != null && !typeStr.isBlank()) {
            try {
                ReportType typeFilter = ReportType.valueOf(typeStr.toUpperCase());
                reports = reports.stream()
                        .filter(r -> r.getType() == typeFilter)
                        .toList();
            } catch (IllegalArgumentException e) {
                // 타입 값이 이상하면 무시하고 진행
            }
        }

        // (3) 사용자 정보 조회 (신고자 정보 미리 가져오기)
        List<Long> reporterIds = reports.stream().map(ReportEntity::getReporterId).distinct().toList();
        Map<Long, UserEntity> reporterMap = reporterIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(reporterIds).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        // (4) DTO 변환 (여기에 작성자 조회 로직 추가!)
        List<AdminReportListResponseDTO.AdminReportItemDTO> list = reports.stream()
                .map(report -> {
                    // A. 신고자(Reporter) 정보 가져오기
                    UserEntity reporter = reporterMap.get(report.getReporterId());

                    // B. [추가됨] 신고 당한 글/댓글의 작성자(Target Author) 찾기
                    String targetAuthorHandle = "알 수 없음"; // 기본값 (삭제된 경우 등)

                    if (report.getType() == ReportType.POST) {
                        // 게시글 조회 -> 작성자 ID 확인 -> 유저 닉네임 조회
                        targetAuthorHandle = postRepository.findById(report.getTargetId())
                                .flatMap(post -> userRepository.findById(post.getUserId()))
                                .map(UserEntity::getNickname) // [수정] getHandle -> getNickname
                                .orElse("삭제된 게시글");
                    }
                    else if (report.getType() == ReportType.COMMENT) {
                        // 댓글 조회 -> 작성자 ID 확인 -> 유저 닉네임 조회
                        targetAuthorHandle = commentRepository.findById(report.getTargetId())
                                .flatMap(comment -> userRepository.findById(comment.getUserId()))
                                .map(UserEntity::getNickname) // [수정] getHandle -> getNickname
                                .orElse("삭제된 댓글");
                    }

                    return AdminReportListResponseDTO.AdminReportItemDTO.builder()
                            .reportId(report.getReportId())
                            .reporterHandle(reporter != null ? reporter.getNickname() : "알 수 없음") // 신고자 닉네임
                            .targetAuthorHandle(targetAuthorHandle) // [수정] 여기에 위에서 구한 변수를 넣어야 함!
                            // .map(UserEntity::getNickname) <--- [삭제] 이 줄이 에러 원인이었습니다.
                            .type(report.getType())
                            .targetId(report.getTargetId())
                            .reason(report.getReason())
                            .status(report.getStatus())
                            .createdAt(report.getCreatedAt())
                            .build();
                })
                .toList();

        return AdminReportListResponseDTO.builder().total(list.size()).list(list).build();
    }

    // 2. 신고 상태 변경 및 후속 처리
    @Transactional
    public void updateReportStatus(Long reportId, ReportStatusUpdateRequestDTO request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (newStatus == ReportStatus.REJECTED) {
            report.reject();
        }
        else if (newStatus == ReportStatus.PROCESSED) {
            processReportAndContent(report);
        }
    }

    // [내부 메서드] 신고 처리 승인 시 로직
    private void processReportAndContent(ReportEntity currentReport) {
        currentReport.process(); // 신고 상태 'PROCESSED'로 변경

        if (currentReport.getType() == ReportType.POST) {
            PostEntity post = postRepository.findById(currentReport.getTargetId()).orElse(null);

            // [핵심 방어] 게시글이 존재하고, '공지사항(isNotice)'이 아닐 때만 삭제!
            if (post != null && !Boolean.TRUE.equals(post.getIsNotice())) {
                post.delete();
            }
        }
        else if (currentReport.getType() == ReportType.COMMENT) {
            CommentEntity comment = commentRepository.findById(currentReport.getTargetId()).orElse(null);
            if (comment != null) {
                comment.delete();
            }
        }

        // 연관된 다른 신고들도 일괄 처리
        List<ReportEntity> relatedReports = reportRepository.findByTargetIdAndType(
                currentReport.getTargetId(), currentReport.getType()
        );
        for (ReportEntity related : relatedReports) {
            related.process();
        }
    }
}