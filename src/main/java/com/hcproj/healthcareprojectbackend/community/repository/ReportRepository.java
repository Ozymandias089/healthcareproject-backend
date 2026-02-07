package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.ReportEntity;
import com.hcproj.healthcareprojectbackend.community.entity.ReportStatus;
import com.hcproj.healthcareprojectbackend.community.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * 신고({@link ReportEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>관리자 신고 큐 조회(상태 기준)</li>
 *   <li>대상(게시글/댓글) 삭제 시 연관 신고 조회</li>
 *   <li>중복 신고 방지(존재 여부 체크)</li>
 * </ul>
 */
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    /**
     * 상태별 신고 목록을 생성일 내림차순으로 조회한다.
     *
     * @param status 신고 상태
     * @return 신고 목록
     */
    List<ReportEntity> findAllByStatusOrderByCreatedAtDesc(ReportStatus status);
    List<ReportEntity> findAllByTypeOrderByCreatedAtDesc(ReportType type);
    List<ReportEntity> findAllByStatusAndTypeOrderByCreatedAtDesc(ReportStatus status, ReportType type);
    List<ReportEntity> findAllByOrderByCreatedAtDesc();


    /**
     * 특정 대상(게시글/댓글)에 대한 신고 목록을 조회한다.
     *
     * @param targetId 대상 ID
     * @param type     신고 대상 타입(POST/COMMENT)
     * @return 대상에 대한 신고 목록
     */
    List<ReportEntity> findByTargetIdAndType(Long targetId, ReportType type);

    /**
     * 동일 신고자에 의한 동일 대상 중복 신고 여부를 확인한다.
     *
     * @param reporterId 신고자 ID
     * @param targetId   대상 ID
     * @param type       신고 대상 타입
     * @return 이미 신고가 존재하면 true
     */
    boolean existsByReporterIdAndTargetIdAndType(Long reporterId, Long targetId, ReportType type);

    List<ReportEntity> findByTargetIdAndTypeAndStatus(Long targetId, ReportType type, ReportStatus status);

    // [추가] 상태별 개수 (대기중인 신고 셀 때 사용)
    long countByStatus(ReportStatus status);

    // [추가] 특정 날짜 이후 생성된 개수 (오늘 들어온 신고 셀 때 사용)
    long countByCreatedAtAfter(Instant startOfDay);
}