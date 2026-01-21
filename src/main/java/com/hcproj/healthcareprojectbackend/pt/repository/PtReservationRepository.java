package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PtReservationRepository extends JpaRepository<PtReservationEntity, Long> {
    Optional<PtReservationEntity> findByPtRoomIdAndUserId(Long ptRoomId, Long userId);
    List<PtReservationEntity> findAllByPtRoomId(Long ptRoomId);

    //특정 유저의 모든 예약 내역을 조회하는 메서드
    List<PtReservationEntity> findAllByUserId(Long userId);
    //권한 체크용 (특정 방에 특정 상태로 예약이 존재하는지 확인)
    boolean existsByPtRoomIdAndUserIdAndStatus(Long ptRoomId, Long userId, PtReservationStatus status);
    //특정 방의 현재 예약된 인원 수 계산 (정원 체크용)
    long countByPtRoomIdAndStatus(Long ptRoomId, PtReservationStatus status);
}