package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PtReservationRepository extends JpaRepository<PtReservationEntity, Long> {
    Optional<PtReservationEntity> findByPtRoomIdAndUserId(Long ptRoomId, Long userId);
    List<PtReservationEntity> findAllByPtRoomId(Long ptRoomId);
}
