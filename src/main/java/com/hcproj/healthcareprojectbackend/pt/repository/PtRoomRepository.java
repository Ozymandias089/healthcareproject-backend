package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * PT 방({@link PtRoomEntity})에 대한 영속성 접근 인터페이스.
 */
public interface PtRoomRepository extends JpaRepository<PtRoomEntity, Long> {

    /** 상태별 방 개수를 반환한다(관리자/대시보드 통계용). */
    long countByStatus(PtRoomStatus status);

    // ============================================================
    // 목록 조회 - 기존 JPQL 유지 (Admin 등에서 사용)
    // ============================================================
    @Query("""
        SELECT DISTINCT p
        FROM PtRoomEntity p
        JOIN UserEntity u ON u.id = p.trainerId
        WHERE (:cursorId IS NULL OR p.ptRoomId < :cursorId)
          AND (:statuses IS NULL OR p.status IN :statuses)
          AND (:trainerId IS NULL OR p.trainerId = :trainerId)
          AND (:roomIds IS NULL OR p.ptRoomId IN :roomIds)
          AND (
              :q IS NULL OR :q = ''
              OR lower(p.title) LIKE lower(concat('%', :q, '%'))
              OR lower(u.nickname) LIKE lower(concat('%', :q, '%'))
              OR lower(u.handle) LIKE lower(concat('%', :q, '%'))
          )
        ORDER BY p.ptRoomId DESC
    """)
    List<PtRoomEntity> findPtRoomsByFilters(
            @Param("cursorId") Long cursorId,
            @Param("statuses") List<PtRoomStatus> statuses,
            @Param("trainerId") Long trainerId,
            @Param("roomIds") List<Long> roomIds,
            @Param("q") String q,
            Pageable pageable
    );

    // ============================================================
    // 검색 (Native Query) - LIVE 탭
    // ============================================================
    @Query(value = "SELECT DISTINCT p.* FROM pt_rooms p " +
            "JOIN users u ON u.user_id = p.trainer_id " +
            "WHERE (:cursorId IS NULL OR p.pt_room_id < :cursorId) " +
            "AND p.status = 'LIVE' " +
            "AND (" +
            "  REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.handle), ' ', '') LIKE :keyword " +
            ") " +
            "ORDER BY p.pt_room_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PtRoomEntity> findPtRoomsLiveWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 검색 (Native Query) - RESERVED 탭
    // ============================================================
    @Query(value = "SELECT DISTINCT p.* FROM pt_rooms p " +
            "JOIN users u ON u.user_id = p.trainer_id " +
            "WHERE (:cursorId IS NULL OR p.pt_room_id < :cursorId) " +
            "AND p.status = 'SCHEDULED' " +
            "AND (" +
            "  REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.handle), ' ', '') LIKE :keyword " +
            ") " +
            "ORDER BY p.pt_room_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PtRoomEntity> findPtRoomsScheduledWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 검색 (Native Query) - ALL 탭 (LIVE + SCHEDULED)
    // ============================================================
    @Query(value = "SELECT DISTINCT p.* FROM pt_rooms p " +
            "JOIN users u ON u.user_id = p.trainer_id " +
            "WHERE (:cursorId IS NULL OR p.pt_room_id < :cursorId) " +
            "AND p.status IN ('LIVE', 'SCHEDULED') " +
            "AND (" +
            "  REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.handle), ' ', '') LIKE :keyword " +
            ") " +
            "ORDER BY p.pt_room_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PtRoomEntity> findPtRoomsAllWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 검색 (Native Query) - MY_PT 탭 (트레이너 본인 방)
    // ============================================================
    @Query(value = "SELECT DISTINCT p.* FROM pt_rooms p " +
            "JOIN users u ON u.user_id = p.trainer_id " +
            "WHERE (:cursorId IS NULL OR p.pt_room_id < :cursorId) " +
            "AND p.trainer_id = :trainerId " +
            "AND (" +
            "  REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.handle), ' ', '') LIKE :keyword " +
            ") " +
            "ORDER BY p.pt_room_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PtRoomEntity> findPtRoomsByTrainerWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("trainerId") Long trainerId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 검색 (Native Query) - MY_JOINED 탭 (참여한 방)
    // ============================================================
    @Query(value = "SELECT DISTINCT p.* FROM pt_rooms p " +
            "JOIN users u ON u.user_id = p.trainer_id " +
            "WHERE (:cursorId IS NULL OR p.pt_room_id < :cursorId) " +
            "AND p.pt_room_id IN :roomIds " +
            "AND (" +
            "  REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "  OR REPLACE(LOWER(u.handle), ' ', '') LIKE :keyword " +
            ") " +
            "ORDER BY p.pt_room_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PtRoomEntity> findPtRoomsByRoomIdsWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("roomIds") List<Long> roomIds,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 기존 메서드들 유지
    // ============================================================

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