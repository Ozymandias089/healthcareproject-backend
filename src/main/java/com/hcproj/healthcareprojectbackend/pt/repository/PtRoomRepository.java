package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * PT 방({@link com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity})
 * 에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 기능</b></p>
 * <ul>
 *   <li>상태별 카운트(대시보드)</li>
 *   <li>커서 기반 목록 조회 + 다양한 필터</li>
 *   <li>동시성 제어를 위한 비관적 락 조회</li>
 *   <li>캘린더 UI를 위한 예약 시작 시각/일간 목록 조회(프로젝션)</li>
 * </ul>
 */
public interface PtRoomRepository extends JpaRepository<PtRoomEntity, Long> {

    /** 상태별 방 개수를 반환한다(관리자/대시보드 통계용). */
    long countByStatus(PtRoomStatus status);

    /**
     * 커서 기반 PT 방 목록 조회 + 필터링.
     *
     * <p><b>커서 규칙</b></p>
     * <ul>
     *   <li>cursorId가 null이면 최신부터 조회</li>
     *   <li>cursorId가 있으면 {@code ptRoomId < cursorId} 조건으로 이전 데이터를 조회</li>
     * </ul>
     *
     * <p><b>필터 규칙</b></p>
     * <ul>
     *   <li>statuses가 null이면 상태 필터 미적용, 아니면 IN 조건 적용</li>
     *   <li>trainerId가 null이면 필터 미적용</li>
     *   <li>roomIds가 null이면 필터 미적용</li>
     *   <li>q가 null/empty면 검색 미적용, 아니면 title LIKE(대소문자 무시) 검색</li>
     * </ul>
     *
     * <p><b>정렬</b></p>
     * ptRoomId 내림차순(최신 우선)
     */
    @Query("SELECT p FROM PtRoomEntity p " +
            "WHERE (:cursorId IS NULL OR p.ptRoomId < :cursorId) " +
            "AND (:statuses IS NULL OR p.status IN :statuses) " +
            "AND (:trainerId IS NULL OR p.trainerId = :trainerId) " +
            "AND (:roomIds IS NULL OR p.ptRoomId IN :roomIds) " +
            "AND (:q IS NULL OR :q = '' OR lower(p.title) LIKE lower(concat('%', :q, '%'))) " +
            "ORDER BY p.ptRoomId DESC")
    List<PtRoomEntity> findPtRoomsByFilters(
            @Param("cursorId") Long cursorId,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("trainerId") Long trainerId,
            @Param("roomIds") List<Long> roomIds,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * 특정 PT 방 row를 비관적 락(PESSIMISTIC_WRITE)으로 조회한다.
     *
     * <p>
     * 방 시작/종료/참가 등 상태 변경이 동시 요청으로 충돌할 수 있는 구간에서
     * SELECT ... FOR UPDATE 용도로 사용한다.
     * </p>
     *
     * @param ptRoomId PT 방 ID
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PtRoomEntity r WHERE r.ptRoomId = :ptRoomId")
    Optional<PtRoomEntity> findByIdForUpdate(@Param("ptRoomId") Long ptRoomId);

    /**
     * 사용자 캘린더 표시용: 예약 PT 시작 시각 목록을 조회한다.
     *
     * <p><b>포함 조건</b></p>
     * <ul>
     *   <li>roomType이 RESERVED</li>
     *   <li>scheduledStartAt이 [startInclusive, endExclusive) 범위</li>
     *   <li>방 상태가 statuses에 포함</li>
     *   <li>사용자가 트레이너이거나(방 소유자), 참가자 조건을 만족하는 경우만 포함</li>
     * </ul>
     *
     * @return 예약 시작 시각 목록(중복 제거)
     */
    @Query("""
    SELECT DISTINCT r.scheduledStartAt
    FROM PtRoomEntity r
    LEFT JOIN PtRoomParticipantEntity p
           ON p.ptRoomId = r.ptRoomId
          AND p.userId = :userId
          AND p.status = :participantStatus
    WHERE r.roomType = :roomType
      AND r.scheduledStartAt >= :startInclusive
      AND r.scheduledStartAt < :endExclusive
      AND r.status IN :statuses
      AND (r.trainerId = :userId OR p.ptRoomParticipantId IS NOT NULL)
""")
    List<Instant> findReservedStartAtsInRangeForUserCalendar(
            @Param("userId") Long userId,
            @Param("participantStatus") PtParticipantStatus participantStatus,
            @Param("roomType") PtRoomType roomType,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

    /**
     * 사용자 일간(하루) PT 목록 조회(캘린더 상세/리스트용).
     *
     * <p>
     * {@link DailyVideoPtRow} 프로젝션으로 필요한 컬럼만 조회하여 성능을 최적화한다.
     * </p>
     *
     * <p><b>정렬</b></p>
     * scheduledStartAt 오름차순
     */
    @Query("""
    SELECT r.ptRoomId AS ptRoomId,
           r.scheduledStartAt AS scheduledStartAt,
           r.title AS title,
           r.status AS status
    FROM PtRoomEntity r
    LEFT JOIN PtRoomParticipantEntity p
           ON p.ptRoomId = r.ptRoomId
          AND p.userId = :userId
          AND p.status = :participantStatus
    WHERE r.roomType = :roomType
      AND r.scheduledStartAt >= :startInclusive
      AND r.scheduledStartAt < :endExclusive
      AND r.status IN :statuses
      AND (r.trainerId = :userId OR p.ptRoomParticipantId IS NOT NULL)
    ORDER BY r.scheduledStartAt ASC
""")
    List<DailyVideoPtRow> findDailyVideoPtRowsForUser(
            @Param("userId") Long userId,
            @Param("participantStatus") PtParticipantStatus participantStatus,
            @Param("roomType") PtRoomType roomType,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

}
