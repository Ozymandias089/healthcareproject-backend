package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtReservationCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtReservationResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtReservationRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PtReservationService {

    private final PtReservationRepository ptReservationRepository;
    private final PtRoomRepository ptRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public PtReservationResponseDTO createReservation(Long ptRoomId, Long userId, PtReservationCreateRequestDTO request) {
        // 1. 방 존재 및 상태 확인
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 예약 가능한 상태인지 확인 (SCHEDULED, LIVE만 가능)
        if (room.getStatus() == PtRoomStatus.ENDED || room.getStatus() == PtRoomStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.RESERVATION_NOT_ALLOWED);
        }

        // 2. Private 방 검증 (로직 단순화: null도 그냥 불일치로 처리)
        if (Boolean.TRUE.equals(room.getIsPrivate())) {
            // 방의 설정된 코드(room.getEntryCode())는 DB에 저장된 값이므로 null이 아님이 보장됨 (생성 시 체크함)
            // 따라서 .equals(null)을 호출해도 에러가 나지 않고 false가 반환됨 -> 즉시 403 에러 발생
            if (!room.getEntryCode().equals(request.entryCode())) {
                throw new BusinessException(ErrorCode.INVALID_ENTRY_CODE);
            }
        }

        // 3. 정원 초과 체크 (현재 예약된 인원 수 확인)
        long currentCount = ptReservationRepository.countByPtRoomIdAndStatus(ptRoomId, PtReservationStatus.REQUESTED);
        if (currentCount >= room.getMaxParticipants()) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }

        // 4. 중복 예약 처리 (멱등성 & 재예약 정책)
        // Optional로 받아서 변수 분리
        Optional<PtReservationEntity> existingReservation = ptReservationRepository.findByPtRoomIdAndUserId(ptRoomId, userId);

        PtReservationEntity reservation; // 여기서 선언만 함 (값 안 넣음)

        if (existingReservation.isPresent()) {
            // 1) 이미 존재하는 경우: 꺼내서 사용
            reservation = existingReservation.get();

            if (reservation.getStatus() == PtReservationStatus.REQUESTED) {
                return toResponse(reservation);
            } else if (reservation.getStatus() == PtReservationStatus.CANCELLED) {
                reservation.recover();
            }
        } else {
            // 2) 없는 경우: 새로 만들어서 할당 (최초 할당)
            reservation = PtReservationEntity.builder()
                    .ptRoomId(ptRoomId)
                    .userId(userId)
                    .status(PtReservationStatus.REQUESTED)
                    .build();
            ptReservationRepository.save(reservation);
        }

        // 여기서 reservation은 무조건 값이 있음이 보장됨
        return toResponse(reservation);
    }

    @Transactional
    public PtReservationResponseDTO cancelReservation(Long ptRoomId, Long userId) {
        // 1. 예약 정보 확인
        PtReservationEntity reservation = ptReservationRepository.findByPtRoomIdAndUserId(ptRoomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 멱등성 처리: 이미 취소된 상태라면 그대로 성공 응답 반환
        if (reservation.getStatus() == PtReservationStatus.CANCELLED) {
            return toResponse(reservation);
        }

        // 3. 방 상태 확인 (정책 B: LIVE 또는 ENDED 상태면 취소 불가)
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (room.getStatus() == PtRoomStatus.LIVE || room.getStatus() == PtRoomStatus.ENDED) {
            throw new BusinessException(ErrorCode.CANCEL_NOT_ALLOWED);
        }

        // 4. 취소 처리
        reservation.cancel(); // Entity의 메서드 호출 (status 변경, cancelledAt 기록)

        return toResponse(reservation);
    }

    //toResponse 메서드도 수정 (cancelledAt 매핑 추가)
    private PtReservationResponseDTO toResponse(PtReservationEntity entity) {
        return PtReservationResponseDTO.builder()
                .ptReservationId(entity.getPtReservationId())
                .ptRoomId(entity.getPtRoomId())
                .status(entity.getStatus())
                .reservedAt(entity.getCreatedAt())
                .cancelledAt(entity.getCancelledAt())
                .build();
    }
}