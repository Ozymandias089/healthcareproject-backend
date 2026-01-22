package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.dto.internal.VideoPtDailyRow;
import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PtRoomRepository extends JpaRepository<PtRoomEntity, Long> {

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
    //여러 방 ID로 방 정보 일괄 조회 (캘린더 PT 정보용)
    List<PtRoomEntity> findAllByPtRoomIdIn(List<Long> ptRoomIds);

    /**
     * 추가: 특정 유저가 특정 상태로 예약한 방들 중,
     * scheduledStartAt이 [startInclusive, endExclusive) 범위에 속하는 시작시각만 조회
     * <p>
     * - 엔티티 연관관계 없이도 cross join 형태로 JPQL 조인 가능
     * - 달력 표시(존재여부) 목적이라 시작시각만 가져오면 됨
     */
    @Query("""
        select r.scheduledStartAt
        from PtRoomEntity r, PtReservationEntity res
        where res.ptRoomId = r.ptRoomId
          and res.userId = :userId
          and res.status = :status
          and r.scheduledStartAt is not null
          and r.scheduledStartAt >= :startInclusive
          and r.scheduledStartAt <  :endExclusive
    """)
    List<Instant> findReservedStartAtsInRange(
            @Param("userId") Long userId,
            @Param("status") PtReservationStatus status,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

    @Query("""
    select new com.hcproj.healthcareprojectbackend.pt.dto.internal.VideoPtDailyRow(
        u.nickname,
        r.scheduledStartAt
    )
    from PtRoomEntity r, PtReservationEntity res, UserEntity u
    where res.ptRoomId = r.ptRoomId
      and res.userId = :userId
      and res.status = :status
      and u.id = r.trainerId
      and r.scheduledStartAt is not null
      and r.scheduledStartAt >= :startInclusive
      and r.scheduledStartAt <  :endExclusive
    order by r.scheduledStartAt asc
""")
    List<VideoPtDailyRow> findDailyVideoPtRows(
            @Param("userId") Long userId,
            @Param("status") PtReservationStatus status,
            @Param("startInclusive") Instant startInclusive,
            @Param("endExclusive") Instant endExclusive
    );

}