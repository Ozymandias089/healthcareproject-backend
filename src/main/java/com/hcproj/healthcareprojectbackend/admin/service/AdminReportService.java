package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.ReportStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.Admincommentdetailresponsedto;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminReportListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.entity.*;
import com.hcproj.healthcareprojectbackend.community.repository.CommentRepository;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PtRoomRepository ptRoomRepository;

    @Transactional(readOnly = true)
    public AdminReportListResponseDTO getReportList(String statusStr, String typeStr) {
        ReportStatus status = parseEnumOrNull(ReportStatus.class, statusStr);
        ReportType type = parseEnumOrNull(ReportType.class, typeStr);

        // 1) reports: DB에서 필터링/정렬
        List<ReportEntity> reports = fetchReports(status, type);
        if (reports.isEmpty()) {
            return AdminReportListResponseDTO.builder().total(0).list(List.of()).build();
        }

        // 2) reporterIds
        List<Long> reporterIds = reports.stream()
                .map(ReportEntity::getReporterId)
                .distinct()
                .toList();

        // 3) 타입별 targetId 모으기
        List<Long> postTargetIds = new ArrayList<>();
        List<Long> commentTargetIds = new ArrayList<>();
        List<Long> roomTargetIds = new ArrayList<>();

        for (ReportEntity r : reports) {
            if (r.getType() == ReportType.POST) postTargetIds.add(r.getTargetId());
            else if (r.getType() == ReportType.COMMENT) commentTargetIds.add(r.getTargetId());
            else if (r.getType() == ReportType.PT_ROOM) roomTargetIds.add(r.getTargetId());
        }

        // 4) targetId -> authorId(trainerId/userId) 매핑 일괄 조회
        Map<Long, Long> postIdToAuthorId = postTargetIds.isEmpty() ? Map.of()
                : postRepository.findIdAndUserIdByIdIn(postTargetIds).stream()
                .collect(Collectors.toMap(
                        PostRepository.IdUserIdProjection::getId,
                        PostRepository.IdUserIdProjection::getUserId
                ));

        Map<Long, Long> commentIdToAuthorId = commentTargetIds.isEmpty() ? Map.of()
                : commentRepository.findIdAndUserIdByIdIn(commentTargetIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.IdUserIdProjection::getId,
                        CommentRepository.IdUserIdProjection::getUserId
                ));

        Map<Long, Long> roomIdToTrainerId = roomTargetIds.isEmpty() ? Map.of()
                : ptRoomRepository.findIdAndTrainerIdByIdIn(roomTargetIds).stream()
                .collect(Collectors.toMap(
                        PtRoomRepository.IdTrainerIdProjection::getId,
                        PtRoomRepository.IdTrainerIdProjection::getTrainerId
                ));

        // 5) reporter + target 작성자 유저를 union 해서 한 번에 조회
        Set<Long> allUserIds = new HashSet<>(reporterIds);
        allUserIds.addAll(postIdToAuthorId.values());
        allUserIds.addAll(commentIdToAuthorId.values());
        allUserIds.addAll(roomIdToTrainerId.values());

        Map<Long, UserRepository.UserHandleNameProjection> allUsers =
                allUserIds.isEmpty()
                        ? Map.of()
                        : userRepository.findByIdIn(allUserIds).stream()
                        .collect(Collectors.toMap(
                                UserRepository.UserHandleNameProjection::getId,
                                u -> u,
                                (a, b) -> a
                        ));


        // 6) DTO 변환
        List<AdminReportListResponseDTO.AdminReportItemDTO> list = reports.stream()
                .map(r -> {
                    UserRepository.UserHandleNameProjection reporter = allUsers.get(r.getReporterId());

                    String reporterHandle = reporter != null ? reporter.getHandle() : "알 수 없음";
                    String reporterName   = reporter != null ? reporter.getNickname() : "알 수 없음";

                    String targetHandle = "알 수 없음";
                    String targetName   = "알 수 없음";

                    Long authorId = null;
                    if (r.getType() == ReportType.POST) authorId = postIdToAuthorId.get(r.getTargetId());
                    else if (r.getType() == ReportType.COMMENT) authorId = commentIdToAuthorId.get(r.getTargetId());
                    else if (r.getType() == ReportType.PT_ROOM) authorId = roomIdToTrainerId.get(r.getTargetId());

                    if (authorId != null) {
                        UserRepository.UserHandleNameProjection target = allUsers.get(authorId);
                        if (target != null) {
                            targetHandle = target.getHandle();
                            targetName   = target.getNickname();
                        }
                    } else {
                        if (r.getType() == ReportType.POST) targetName = "삭제된 게시글";
                        else if (r.getType() == ReportType.COMMENT) targetName = "삭제된 댓글";
                        else if (r.getType() == ReportType.PT_ROOM) targetName = "삭제/종료된 방";
                    }

                    return AdminReportListResponseDTO.AdminReportItemDTO.builder()
                            .reportId(r.getReportId())
                            .reporterHandle(reporterHandle)
                            .reporterName(reporterName)
                            .targetAuthorHandle(targetHandle)
                            .targetAuthorName(targetName)
                            .type(r.getType())
                            .targetId(r.getTargetId())
                            .reason(r.getReason())
                            .status(r.getStatus())
                            .createdAt(r.getCreatedAt())
                            .build();
                })
                .toList();


        return AdminReportListResponseDTO.builder()
                .total(list.size())
                .list(list)
                .build();
    }

    // 2. 신고 상태 변경 및 후속 처리
    @Transactional
    public void updateReportStatus(Long reportId, ReportStatusUpdateRequestDTO request) {
        ReportEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));

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

    // 3. 댓글 상세 조회 (관리자용)
    @Transactional(readOnly = true)
    public Admincommentdetailresponsedto getCommentDetail(Long commentId) {
        // 댓글 조회
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 작성자 조회
        UserEntity author = userRepository.findById(comment.getUserId())
                .orElse(null);

        // 게시글 제목 조회
        String postTitle = postRepository.findById(comment.getPostId())
                .map(PostEntity::getTitle)
                .orElse("삭제된 게시글");

        return new Admincommentdetailresponsedto(
                comment.getCommentId(),
                comment.getPostId(),
                postTitle,
                new Admincommentdetailresponsedto.AuthorDTO(
                        comment.getUserId(),
                        author != null ? author.getNickname() : "알 수 없음",
                        author != null ? author.getHandle() : "unknown"
                ),
                comment.getContent(),
                comment.getStatus(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt()
        );
    }

    // [내부 메서드] 신고 처리 승인 시 로직 (실제 삭제/종료 수행)
    private void processReportAndContent(ReportEntity currentReport) {
        currentReport.process(); // 신고 상태 'PROCESSED'로 변경

        if (currentReport.getType() == ReportType.POST) {
            PostEntity post = postRepository.findById(currentReport.getTargetId()).orElse(null);
            // 게시글이 존재하고, '공지사항'이 아닐 때만 삭제
            if (post != null && !Boolean.TRUE.equals(post.getIsNotice())) {
                post.delete();
            }
        }
        else if (currentReport.getType() == ReportType.COMMENT) {
            commentRepository.findById(currentReport.getTargetId()).ifPresent(CommentEntity::delete);
        }
        else if (currentReport.getType() == ReportType.PT_ROOM) {
            ptRoomRepository.findById(currentReport.getTargetId()).ifPresent(PtRoomEntity::forceClose);
        }

        // 연관된 다른 신고들도 일괄 처리 (같은 타겟에 대한 중복 신고들)
        List<ReportEntity> relatedReports = reportRepository.findByTargetIdAndType(
                currentReport.getTargetId(), currentReport.getType()
        );
        for (ReportEntity related : relatedReports) {
            // 이미 처리된 건 제외하고 처리 상태로 변경
            if (related.getStatus() != ReportStatus.PROCESSED) {
                related.process();
            }
        }
    }

    private List<ReportEntity> fetchReports(ReportStatus status, ReportType type) {
        if (status != null && type != null) {
            return reportRepository.findAllByStatusAndTypeOrderByCreatedAtDesc(status, type);
        }
        if (status != null) {
            return reportRepository.findAllByStatusOrderByCreatedAtDesc(status);
        }
        if (type != null) {
            return reportRepository.findAllByTypeOrderByCreatedAtDesc(type);
        }
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    private static <E extends Enum<E>> E parseEnumOrNull(Class<E> enumType, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}