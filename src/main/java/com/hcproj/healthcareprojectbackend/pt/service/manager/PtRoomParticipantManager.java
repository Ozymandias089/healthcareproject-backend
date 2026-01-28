package com.hcproj.healthcareprojectbackend.pt.service.manager;

import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PtRoomParticipantManager {

    private final PtRoomParticipantRepository ptRoomParticipantRepository;

    /**
     * 트레이너가 참가자 테이블에 JOINED 상태로 존재하도록 보장 (멱등)
     */
    @Transactional
    public void ensureTrainerJoined(Long ptRoomId, Long trainerId) {
        PtRoomParticipantEntity participant = ptRoomParticipantRepository
                .findByPtRoomIdAndUserId(ptRoomId, trainerId)
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

    /**
     * 현재 JOINED 인 참가자들을 LEFT 로 정리 (멱등)
     */
    @Transactional
    public void cleanupJoinedParticipants(Long ptRoomId) {
        List<PtRoomParticipantEntity> participants =
                ptRoomParticipantRepository.findAllByPtRoomId(ptRoomId);

        for (PtRoomParticipantEntity p : participants) {
            if (p.getStatus() == PtParticipantStatus.JOINED) {
                p.exit();
            }
        }
    }
}