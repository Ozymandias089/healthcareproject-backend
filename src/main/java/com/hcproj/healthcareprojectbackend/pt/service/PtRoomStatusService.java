package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomStatusResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PtRoomStatusService {

    private final PtRoomRepository ptRoomRepository;
    private final PtRoomParticipantRepository ptRoomParticipantRepository;
    private final PtJanusRoomKeyService ptJanusRoomKeyService;

    @Transactional
    public PtRoomStatusResponseDTO updateStatus(Long ptRoomId, Long userId, PtRoomStatusUpdateRequestDTO request) {
        PtRoomEntity room = ptRoomRepository.findByIdForUpdate(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!room.getTrainerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        switch (request.status()) {
            case LIVE -> startRoom(room);
            case ENDED -> endRoom(room);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return new PtRoomStatusResponseDTO(room.getPtRoomId(), room.getStatus(), room.getScheduledStartAt());
    }

    private void startRoom(PtRoomEntity room) {
        if (room.getStatus() != PtRoomStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // ✅ LIVE 전이 시 키 할당 (멱등)
        ptJanusRoomKeyService.allocate(room.getPtRoomId());

        // 상태 전이 + startedAt
        room.start();

        // 트레이너 참가 보장
        ensureTrainerJoined(room.getPtRoomId(), room.getTrainerId());
    }

    private void endRoom(PtRoomEntity room) {
        if (room.getStatus() != PtRoomStatus.LIVE) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        room.end(); // ENDED + markDeleted()

        // JOINED → LEFT 정리
        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository.findAllByPtRoomId(room.getPtRoomId());
        for (PtRoomParticipantEntity p : participants) {
            if (p.getStatus() == PtParticipantStatus.JOINED) {
                p.exit();
            }
        }

        // ✅ 키 반납
        ptJanusRoomKeyService.releaseByPtRoomId(room.getPtRoomId());
    }

    private void ensureTrainerJoined(Long ptRoomId, Long trainerId) {
        PtRoomParticipantEntity participant = ptRoomParticipantRepository.findByPtRoomIdAndUserId(ptRoomId, trainerId)
                .orElse(null);

        if (participant == null) {
            participant = PtRoomParticipantEntity.builder()
                    .ptRoomId(ptRoomId)
                    .userId(trainerId)
                    .status(PtParticipantStatus.JOINED)
                    .joinedAt(Instant.now())
                    .build();
            ptRoomParticipantRepository.save(participant);
            return;
        }

        if (participant.getStatus() != PtParticipantStatus.JOINED) {
            participant.join();
            ptRoomParticipantRepository.save(participant);
        }
    }
}
