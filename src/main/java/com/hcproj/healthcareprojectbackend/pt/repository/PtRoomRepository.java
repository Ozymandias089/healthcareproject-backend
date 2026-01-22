package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    // 30000 ~ 39999 사이 빈 키(재사용 가능) 찾기
    @Query(value = """
        WITH RECURSIVE number_series(num) AS (
            SELECT 30000
            UNION ALL
            SELECT num + 1 FROM number_series WHERE num < 39999
        )
        SELECT CAST(num AS CHAR) 
        FROM number_series
        WHERE CAST(num AS CHAR) NOT IN (
            SELECT janus_room_key 
            FROM pt_rooms 
            WHERE janus_room_key IS NOT NULL 
            AND status IN ('LIVE', 'SCHEDULED')
        )
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> findFirstAvailableJanusKey();
}