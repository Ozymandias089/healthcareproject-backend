package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusKeyStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtJanusRoomKeyEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PtJanusRoomKeyRepository extends JpaRepository<PtJanusRoomKeyEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT k FROM PtJanusRoomKeyEntity k
    WHERE k.status = :status
    ORDER BY k.roomKey ASC
""")
    java.util.List<PtJanusRoomKeyEntity> findAvailableForUpdate(
            @Param("status") PtJanusKeyStatus status,
            org.springframework.data.domain.Pageable pageable
    );

    Optional<PtJanusRoomKeyEntity> findByPtRoomId(Long ptRoomId);
}
