package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomStatusResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import com.hcproj.healthcareprojectbackend.pt.service.manager.PtRoomParticipantManager;
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
    private final PtRoomParticipantManager ptRoomParticipantManager;

    @Transactional
    public PtRoomStatusResponseDTO updateStatus(Long ptRoomId, Long userId, PtRoomStatusUpdateRequestDTO request) {
        PtRoomEntity room = ptRoomRepository.findByIdForUpdate(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 권한(도메인 외부 정책)은 서비스가 한다
        if (!room.getTrainerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 상태 전이 자체의 정합성은 도메인 액션이 책임진다
        switch (request.status()) {
            case LIVE -> {
                // 외부 부수효과는 도메인 액션 호출 전/후로 서비스가 조합
                ptJanusRoomKeyService.allocate(room.getPtRoomId()); // 멱등
                room.start();
                ptRoomParticipantManager.ensureTrainerJoined(room.getPtRoomId(), room.getTrainerId());
            }
            case ENDED -> {
                room.end();
                cleanupJoinedParticipants(room.getPtRoomId());
                ptJanusRoomKeyService.releaseByPtRoomId(room.getPtRoomId());
            }
            case CANCELLED -> {
                room.cancel();
                ptJanusRoomKeyService.releaseByPtRoomId(room.getPtRoomId()); // 취소도 키 반납 정책이면
            }
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return new PtRoomStatusResponseDTO(room.getPtRoomId(), room.getStatus(), room.getScheduledStartAt());
    }

    private void cleanupJoinedParticipants(Long ptRoomId) {
        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository.findAllByPtRoomId(ptRoomId);
        for (PtRoomParticipantEntity p : participants) {
            if (p.getStatus() == PtParticipantStatus.JOINED) {
                p.exit();
            }
        }
    }
}
