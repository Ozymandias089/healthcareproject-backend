package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PtRoomParticipantRepository extends JpaRepository<PtRoomParticipantEntity, Long> {
    Optional<PtRoomParticipantEntity> findByPtRoomIdAndUserId(Long ptRoomId, Long userId);
    List<PtRoomParticipantEntity> findAllByPtRoomId(Long ptRoomId);
}
