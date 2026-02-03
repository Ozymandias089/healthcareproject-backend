package com.hcproj.healthcareprojectbackend.pt.service;

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
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PtRoomParticipantService {

    private final PtRoomRepository ptRoomRepository;
    private final UserRepository userRepository;
    private final PtRoomParticipantRepository participantRepository;

    @Transactional
    public PtRoomKickResponseDTO kickParticipant(Long ptRoomId, Long trainerId, PtRoomKickRequestDTO request) {
        validateUserAndStatus(trainerId);
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!room.getTrainerId().equals(trainerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        UserEntity targetUser = userRepository.findByHandle(request.targetHandle())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 자기 자신 강퇴 방지
        if (targetUser.getId().equals(trainerId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        PtRoomParticipantEntity participant = participantRepository.findByPtRoomIdAndUserId(ptRoomId, targetUser.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_JOINED));

        participant.kick();

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
    /**
     * 유저 상태 검증 (SUSPENDED 체크)
     */
    private UserEntity validateUserAndStatus(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }

        return user;
    }
}
