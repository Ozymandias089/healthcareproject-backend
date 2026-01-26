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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 1. 신고 목록 조회 (기존 로직 유지)
    @Transactional(readOnly = true)
    public AdminReportListResponseDTO getReportList(String statusStr) {
        List<ReportEntity> reports;

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

        List<Long> reporterIds = reports.stream().map(ReportEntity::getReporterId).distinct().toList();
        Map<Long, UserEntity> userMap = reporterIds.isEmpty() ? Collections.emptyMap() :
                userRepository.findAllById(reporterIds).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<AdminReportListResponseDTO.AdminReportItemDTO> list = reports.stream()
                .map(report -> {
                    UserEntity reporter = userMap.get(report.getReporterId());
                    return AdminReportListResponseDTO.AdminReportItemDTO.builder()
                            .reportId(report.getReportId())
                            .reporterHandle(reporter != null ? reporter.getHandle() : "unknown")
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

    // 2. 신고 상태 변경 및 후속 처리 (통합 로직)
    @Transactional
    public void updateReportStatus(Long reportId, ReportStatusUpdateRequestDTO request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND)); // 필요시 에러코드 추가

        ReportStatus newStatus;
        try {
            newStatus = ReportStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 상태별 로직 분기
        if (newStatus == ReportStatus.REJECTED) {
            // [반려] -> 해당 신고만 'REJECTED'로 변경하고 끝 (글 삭제 X)
            report.reject();
        }
        else if (newStatus == ReportStatus.PROCESSED) {
            // [처리 승인] -> 글 삭제 + 관련 신고 일괄 처리
            processReportAndContent(report);
        }
    }

    // [내부 메서드] 신고 처리 승인 시 -> 콘텐츠 강제 삭제 및 연관 신고 정리
    private void processReportAndContent(ReportEntity currentReport) {
        // 1. 현재 신고 처리됨 변경
        currentReport.process();

        // 2. 콘텐츠 타입에 따른 강제 삭제
        if (currentReport.getType() == ReportType.POST) {
            PostEntity post = postRepository.findById(currentReport.getTargetId())
                    .orElse(null); // 이미 삭제되었을 수도 있으므로 null 체크

            if (post != null) {
                post.delete(); // 게시글 삭제 (멱등성 보장)
            }
        }
        else if (currentReport.getType() == ReportType.COMMENT) {
            CommentEntity comment = commentRepository.findById(currentReport.getTargetId())
                    .orElse(null);

            if (comment != null) {
                comment.delete(); // 댓글 삭제 (Entity에 delete 메서드 필요)
            }
        }

        // 3. 해당 콘텐츠(TargetId)에 걸려있는 "다른 신고들"도 모두 'PROCESSED'로 일괄 변경
        List<ReportEntity> relatedReports = reportRepository.findByTargetIdAndType(
                currentReport.getTargetId(), currentReport.getType()
        );

        for (ReportEntity related : relatedReports) {
            related.process(); // 이미 처리된 건 멱등성 로직에 의해 무시됨
        }
    }
}