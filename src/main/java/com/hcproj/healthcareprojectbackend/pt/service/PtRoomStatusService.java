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

    @Transactional
    public PtRoomStatusResponseDTO updateStatus(Long ptRoomId, Long userId, PtRoomStatusUpdateRequestDTO request) {
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!room.getTrainerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        switch (request.status()) {
            case LIVE -> startRoom(room);
            case ENDED -> endRoom(room);
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // 그 외 상태 변경은 허용 안 함
        }

        return new PtRoomStatusResponseDTO(
                room.getPtRoomId(),
                room.getStatus(),
                room.getScheduledStartAt()
        );
    }

    private void startRoom(PtRoomEntity room) {
        if (room.getStatus() != PtRoomStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        room.start();
    }

    private void endRoom(PtRoomEntity room) {
        if (room.getStatus() != PtRoomStatus.LIVE) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        room.end();

        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository.findAllByPtRoomId(room.getPtRoomId());

        for (PtRoomParticipantEntity p : participants) {
            if (p.getStatus() == PtParticipantStatus.JOINED) {
                p.exit();
            }
        }
    }
}