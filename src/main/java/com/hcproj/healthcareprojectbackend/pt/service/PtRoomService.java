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
    // PtReservationRepository는 더 이상 deleteRoom에서 사용하지 않으므로 제거했습니다.
    private final UserRepository userRepository;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public PtRoomDetailResponseDTO createRoom(Long userId, PtRoomCreateRequestDTO request) {
        // 1. 유저 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 입력값 검증
        if (request.roomType() == PtRoomType.RESERVED && request.scheduledAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String entryCode = Boolean.TRUE.equals(request.isPrivate()) ? generateEntryCode() : null;
        PtRoomStatus initialStatus = (request.roomType() == PtRoomType.RESERVED) ? PtRoomStatus.SCHEDULED : PtRoomStatus.LIVE;

        // 3. 사용 가능한 빈 Janus Key 조회 (30000 ~ 39999)
        // 삭제/취소된 방의 키(NULL로 반납된 것)도 여기서 다시 찾아짐
        String availableKey = ptRoomRepository.findFirstAvailableJanusKey()
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_FULL)); // 가용 범위 초과 시 에러

        // 4. Entity 생성
        PtRoomEntity ptRoom = PtRoomEntity.builder()
                .trainerId(userId)
                .title(request.title())
                .description(request.description())
                .roomType(request.roomType())
                .scheduledStartAt(request.scheduledAt())
                .maxParticipants(request.maxParticipants())
                .isPrivate(request.isPrivate())
                .entryCode(entryCode)
                .status(initialStatus)
                .build();

        // 5. 키 할당 (비즈니스 메서드 사용)
        ptRoom.assignJanusKey(availableKey);

        // 6. 저장
        PtRoomEntity savedRoom = ptRoomRepository.save(ptRoom);

        // 7. 참여자 등록
        PtRoomParticipantEntity participant = PtRoomParticipantEntity.builder()
                .ptRoomId(savedRoom.getPtRoomId())
                .userId(userId)
                .status(PtParticipantStatus.JOINED)
                .joinedAt(Instant.now())
                .build();

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

    // [수정됨] 방 삭제 (Soft Delete + Key Release)
    @Transactional
    public void deleteRoom(Long ptRoomId, Long userId) {
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 1. 권한 확인: 방장(트레이너) 본인만 삭제 가능
        if (!room.getTrainerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 2. 방 상태를 '취소'로 변경 (캘린더 히스토리용으로 데이터 보존)
        room.cancel();

        // 3. Janus Key 반납 (NULL 처리 -> 30000번대 번호 즉시 재사용 가능)
        room.releaseJanusKey();

        // 예약 내역(Reservation)이나 참여자(Participant) 내역은 삭제하지 않고 남겨둡니다.
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
                .janusRoomKey(room.getJanusRoomKey()) // 재사용된 키가 반환됨
                .maxParticipants(room.getMaxParticipants())
                .participants(new PtRoomDetailResponseDTO.ParticipantsDTO(1, List.of(participantUser)))
                .build();
    }
}