package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}