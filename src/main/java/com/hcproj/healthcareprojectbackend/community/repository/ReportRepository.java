package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.ReportEntity;
import com.hcproj.healthcareprojectbackend.community.entity.ReportStatus;
import com.hcproj.healthcareprojectbackend.community.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    // 관리자용: 상태별 조회 (PENDING인 것만 모아보기 등)
    List<ReportEntity> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);

    // [추가] 특정 대상(게시글/댓글)에 대한 신고 목록 조회 (삭제 시 일괄 처리를 위해 필요)
    List<ReportEntity> findByTargetIdAndType(Long targetId, ReportType type);

    // [추가] 중복 신고 체크용
    boolean existsByReporterIdAndTargetIdAndType(Long reporterId, Long targetId, ReportType type);
}