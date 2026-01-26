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

public interface PtRoomRepository extends JpaRepository<PtRoomEntity, Long> {

    // [추가됨] 상태별 방 개수 카운트 (대시보드용)
    long countByStatus(PtRoomStatus status);

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

    // 비관적 락: 방 row를 SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PtRoomEntity r WHERE r.ptRoomId = :ptRoomId")
    Optional<PtRoomEntity> findByIdForUpdate(@Param("ptRoomId") Long ptRoomId);

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
