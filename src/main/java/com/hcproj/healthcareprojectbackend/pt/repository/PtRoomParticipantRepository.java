package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PtRoomParticipantRepository extends JpaRepository<PtRoomParticipantEntity, Long> {
    Optional<PtRoomParticipantEntity> findByPtRoomIdAndUserId(Long ptRoomId, Long userId);

    List<PtRoomParticipantEntity> findAllByPtRoomId(Long ptRoomId);

    // 특정 방의 특정 상태인 참여자를 입장 순서대로 조회
    List<PtRoomParticipantEntity> findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(Long ptRoomId, PtParticipantStatus status);

    long countByPtRoomIdAndStatus(Long ptRoomId, PtParticipantStatus status);
}
