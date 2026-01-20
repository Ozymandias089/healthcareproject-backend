package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant; //
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
    public PtRoomCreateResponseDTO createRoom(Long userId, PtRoomCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // [참고] 권한 검증은 필요 시 주석 해제하여 사용
        // if (user.getRole() != UserRole.TRAINER) throw new BusinessException(ErrorCode.FORBIDDEN);

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

        // 트레이너 자동 참여 등록 (joinedAt은 Instant 사용)
        PtRoomParticipantEntity participant = PtRoomParticipantEntity.builder()
                .ptRoomId(savedRoom.getPtRoomId()).userId(userId)
                .status(PtParticipantStatus.LIVE).joinedAt(Instant.now()).build();

        ptRoomParticipantRepository.save(participant);

        return assembleCreateResponse(savedRoom, user, entryCode);
    }

    private String generateEntryCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    private PtRoomCreateResponseDTO assembleCreateResponse(PtRoomEntity room, UserEntity trainer, String entryCode) {
        var trainerDTO = new PtRoomCreateResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle());
        var participantUser = new PtRoomCreateResponseDTO.ParticipantUserDTO(trainer.getNickname(), trainer.getHandle());

        return PtRoomCreateResponseDTO.builder()
                .ptRoomId(room.getPtRoomId()).title(room.getTitle()).description(room.getDescription())
                .scheduledAt(room.getScheduledStartAt()).trainer(trainerDTO).entryCode(entryCode)
                .isPrivate(room.getIsPrivate()).roomType(room.getRoomType()).status(room.getStatus())
                .maxParticipants(room.getMaxParticipants())
                .participants(new PtRoomCreateResponseDTO.ParticipantsDTO(1, List.of(participantUser)))
                .build();
    }
}