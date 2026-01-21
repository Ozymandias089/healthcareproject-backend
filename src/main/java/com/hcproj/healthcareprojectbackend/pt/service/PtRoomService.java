package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomEntryRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PtRoomService {

    private final PtRoomRepository ptRoomRepository;
    private final PtRoomParticipantRepository ptRoomParticipantRepository;
    private final UserRepository userRepository;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public PtRoomDetailResponseDTO createRoom(Long userId, PtRoomCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.roomType() == PtRoomType.RESERVED && request.scheduledAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String entryCode = Boolean.TRUE.equals(request.isPrivate()) ? generateEntryCode() : null;

        PtRoomStatus initialStatus = (request.roomType() == PtRoomType.RESERVED) ? PtRoomStatus.SCHEDULED : PtRoomStatus.LIVE;

        PtRoomEntity ptRoom = PtRoomEntity.builder()
                .trainerId(userId).title(request.title()).description(request.description())
                .roomType(request.roomType()).scheduledStartAt(request.scheduledAt())
                .maxParticipants(request.maxParticipants()).isPrivate(request.isPrivate())
                .entryCode(entryCode).status(initialStatus).build();

        PtRoomEntity savedRoom = ptRoomRepository.save(ptRoom);

        PtRoomParticipantEntity participant = PtRoomParticipantEntity.builder()
                .ptRoomId(savedRoom.getPtRoomId()).userId(userId)
                .status(PtParticipantStatus.JOINED)
                .joinedAt(Instant.now()).build();

        ptRoomParticipantRepository.save(participant);

        return assembleCreateResponse(savedRoom, user, entryCode);
    }

    @Transactional
    public void joinRoom(Long ptRoomId, Long userId, PtRoomEntryRequestDTO request) {
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (Boolean.TRUE.equals(room.getIsPrivate())) {
            if (request.entryCode() == null || !request.entryCode().equals(room.getEntryCode())) {
                throw new BusinessException(ErrorCode.INVALID_ENTRY_CODE);
            }
        }

        PtRoomParticipantEntity participant = ptRoomParticipantRepository.findByPtRoomIdAndUserId(ptRoomId, userId)
                .orElse(PtRoomParticipantEntity.builder()
                        .ptRoomId(ptRoomId)
                        .userId(userId)
                        .status(PtParticipantStatus.SCHEDULED)
                        .joinedAt(Instant.now())
                        .build());

        participant.join();

        ptRoomParticipantRepository.save(participant);
    }

    @Transactional
    public void leaveRoom(Long ptRoomId, Long userId) {
        PtRoomParticipantEntity participant = ptRoomParticipantRepository.findByPtRoomIdAndUserId(ptRoomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        participant.exit();

        ptRoomParticipantRepository.save(participant);
    }

    private String generateEntryCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    private PtRoomDetailResponseDTO assembleCreateResponse(PtRoomEntity room, UserEntity trainer, String entryCode) {
        var trainerDTO = new PtRoomDetailResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle(), null);
        var participantUser = new PtRoomDetailResponseDTO.UserDTO(trainer.getNickname(), trainer.getHandle());

        return PtRoomDetailResponseDTO.builder()
                .ptRoomId(room.getPtRoomId())
                .title(room.getTitle())
                .description(room.getDescription())
                .scheduledAt(room.getScheduledStartAt())
                .trainer(trainerDTO)
                .entryCode(entryCode)
                .isPrivate(room.getIsPrivate())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .janusRoomKey(room.getJanusRoomKey())
                .maxParticipants(room.getMaxParticipants())
                .participants(new PtRoomDetailResponseDTO.ParticipantsDTO(1, List.of(participantUser)))
                .build();
    }
}