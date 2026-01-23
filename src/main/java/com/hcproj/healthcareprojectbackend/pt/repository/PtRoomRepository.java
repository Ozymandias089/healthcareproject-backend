package com.hcproj.healthcareprojectbackend.pt.repository;

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
            "ORDER BY p.ptRoomId DESC")
    List<PtRoomEntity> findPtRoomsByFilters(
            @Param("cursorId") Long cursorId,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("trainerId") Long trainerId,
            @Param("roomIds") List<Long> roomIds,
            Pageable pageable
    );

    List<PtRoomEntity> findAllByTrainerId(Long trainerId);

    // 여러 방 ID로 방 정보 일괄 조회 (캘린더 PT 정보용)
    List<PtRoomEntity> findAllByPtRoomIdIn(List<Long> ptRoomIds);

    // ✅ 비관적 락: 방 row를 SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PtRoomEntity r WHERE r.ptRoomId = :ptRoomId")
    Optional<PtRoomEntity> findByIdForUpdate(@Param("ptRoomId") Long ptRoomId);

    // =========================
    // ✅ 캘린더용 (예약 제거 대응)
    // =========================

    // 1) 범위 내 "내가 트레이너인 예약형 PT"의 scheduledStartAt 목록
    @Query("""
        SELECT r.scheduledStartAt
        FROM PtRoomEntity r
        WHERE r.trainerId = :trainerId
          AND r.roomType = :roomType
          AND r.scheduledStartAt >= :startInclusive
          AND r.scheduledStartAt < :endExclusive
          AND r.status IN :statuses
    """)
    List<Instant> findReservedStartAtsInRangeForTrainer(
            @Param("trainerId") Long trainerId,
            @Param("roomType") PtRoomType roomType,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

    // 2) 일간 상세 표시용 row 목록(여러 개면 summary에서 "외 n건" 처리 가능)
    @Query("""
    SELECT r.ptRoomId AS ptRoomId,
           r.scheduledStartAt AS scheduledStartAt,
           r.title AS title
    FROM PtRoomEntity r
    WHERE r.trainerId = :trainerId
      AND r.roomType = :roomType
      AND r.scheduledStartAt >= :startInclusive
      AND r.scheduledStartAt < :endExclusive
      AND r.status IN :statuses
    ORDER BY r.scheduledStartAt ASC
""")
    List<DailyVideoPtRow> findDailyVideoPtRowsForTrainer(
            @Param("trainerId") Long trainerId,
            @Param("roomType") PtRoomType roomType,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );
}
