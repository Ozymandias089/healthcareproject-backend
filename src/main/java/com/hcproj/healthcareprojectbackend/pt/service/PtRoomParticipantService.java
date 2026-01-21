package com.hcproj.healthcareprojectbackend.pt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomKickRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomKickResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.PtParticipantStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomParticipantEntity;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PtRoomParticipantService {
    private final PtRoomRepository ptRoomRepository;
    private final UserRepository userRepository;
    private final PtRoomParticipantRepository participantRepository;

    @Transactional
    public PtRoomKickResponseDTO kickParticipant(Long ptRoomId, Long trainerId, PtRoomKickRequestDTO request) {
        // 1. 방 및 트레이너 권한 확인
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // ★ [임시 주석 처리] 트레이너 권한 체크
        // if (!room.getTrainerId().equals(trainerId)) {
        //    throw new BusinessException(ErrorCode.FORBIDDEN);
        // }

        // 2. 강퇴 대상 유저 확인
        UserEntity targetUser = userRepository.findByHandle(request.targetHandle())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // ★ [임시 주석 처리] 자기 자신 강퇴 방지 (테스트를 위해)
        // 자기 자신 강퇴 방지
        // if (targetUser.getId().equals(trainerId)) {
        //    throw new BusinessException(ErrorCode.INVALID_REQUEST);
        // }

        // 3. 참여 기록 확인
        PtRoomParticipantEntity participant = participantRepository.findByPtRoomIdAndUserId(ptRoomId, targetUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOINED));

        // 4. 엔티티를 통한 강퇴 처리 (내부에서 멱등성 로직 실행)
        participant.kick();

        // 5. 남은 참여 인원 수 계산 (JOINED 상태인 인원)
        long joinedCount = participantRepository.countByPtRoomIdAndStatus(ptRoomId, PtParticipantStatus.JOINED);

        return PtRoomKickResponseDTO.builder()
                .ptRoomId(ptRoomId)
                .kickedUser(new PtRoomKickResponseDTO.KickedUserDTO(
                        targetUser.getHandle(),
                        targetUser.getNickname(),
                        targetUser.getProfileImageUrl()
                ))
                .kickedAt(participant.getLeftAt())
                .participants(new PtRoomKickResponseDTO.ParticipantCountDTO(joinedCount))
                .build();
    }
}
