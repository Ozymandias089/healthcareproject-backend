package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.entity.ReportEntity;
import com.hcproj.healthcareprojectbackend.community.entity.ReportStatus;
import com.hcproj.healthcareprojectbackend.community.entity.ReportType;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomEntryRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO; // [Import 추가]
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import com.hcproj.healthcareprojectbackend.pt.service.manager.PtRoomParticipantManager;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest; // [Import 추가]
import org.springframework.data.domain.Pageable; // [Import 추가]
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map; // [Import 추가]
import java.util.Set; // [Import 추가]
import java.util.function.Function; // [Import 추가]
import java.util.stream.Collectors; // [Import 추가]

@Service
@RequiredArgsConstructor
public class PtRoomService {

    private final PtRoomRepository ptRoomRepository;
    private final PtRoomParticipantRepository ptRoomParticipantRepository;
    private final UserRepository userRepository;
    private final PtJanusRoomKeyService ptJanusRoomKeyService;
    private final TrainerInfoRepository trainerInfoRepository;
    private final ReportRepository reportRepository;
    private final PtRoomParticipantManager ptRoomParticipantManager;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public PtRoomDetailResponseDTO createRoom(Long userId, PtRoomCreateRequestDTO request) {
        UserEntity trainer = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String bio = trainerInfoRepository.findBioByTrainerId(trainer.getId()).orElse(null);

        if (request.roomType() == PtRoomType.RESERVED && request.scheduledAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String entryCode = Boolean.TRUE.equals(request.isPrivate()) ? generateEntryCode() : null;

        PtRoomStatus initialStatus = (request.roomType() == PtRoomType.RESERVED)
                ? PtRoomStatus.SCHEDULED
                : PtRoomStatus.LIVE;

        Instant startedAt = (initialStatus == PtRoomStatus.LIVE) ? Instant.now() : null;

        PtRoomEntity room = PtRoomEntity.builder()
                .trainerId(userId)
                .title(request.title())
                .description(request.description())
                .roomType(request.roomType())
                .scheduledStartAt(request.scheduledAt())
                .startedAt(startedAt)
                .maxParticipants(request.maxParticipants())
                .isPrivate(request.isPrivate())
                .entryCode(entryCode)
                .status(initialStatus)
                .build();

        PtRoomEntity savedRoom = ptRoomRepository.save(room);

        // ✅ LIVE 생성이면 키 할당 + 트레이너 참가 보장
        Integer janusKey = null;
        if (savedRoom.getStatus() == PtRoomStatus.LIVE) {
            janusKey = ptJanusRoomKeyService.allocate(savedRoom.getPtRoomId()).getRoomKey();
            ptRoomParticipantManager.ensureTrainerJoined(savedRoom.getPtRoomId(), userId);
        }

        return assembleCreateResponse(savedRoom, trainer, entryCode, janusKey, bio);
    }

    @Transactional
    public void joinRoom(Long ptRoomId, Long userId, PtRoomEntryRequestDTO request) {
        // ✅ 방 row 잠금 (정원 체크 동시성 방지)
        PtRoomEntity room = ptRoomRepository.findByIdForUpdate(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 정책: LIVE만 join 허용
        if (room.getStatus() != PtRoomStatus.LIVE) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // private 코드 검증
        if (Boolean.TRUE.equals(room.getIsPrivate())) {
            if (request == null || request.entryCode() == null || !request.entryCode().equals(room.getEntryCode())) {
                throw new BusinessException(ErrorCode.INVALID_ENTRY_CODE);
            }
        }

        // 멱등: 이미 JOINED면 성공
        PtRoomParticipantEntity existing = ptRoomParticipantRepository.findByPtRoomIdAndUserId(ptRoomId, userId)
                .orElse(null);
        if (existing != null && existing.getStatus() == PtParticipantStatus.JOINED) {
            return;
        }

        if (existing != null && existing.getStatus() == PtParticipantStatus.KICKED) {
            throw new BusinessException(ErrorCode.KICKED_USER);
        }

        // ✅ 정원 체크 (락 잡힌 상태에서)
        long joinedCount = ptRoomParticipantRepository.countByPtRoomIdAndStatus(ptRoomId, PtParticipantStatus.JOINED);
        Integer max = room.getMaxParticipants();
        if (max != null && joinedCount >= max) {
            throw new BusinessException(ErrorCode.ROOM_FULL);
        }

        // 참가 처리
        if (existing == null) {
            PtRoomParticipantEntity participant = PtRoomParticipantEntity.builder()
                    .ptRoomId(ptRoomId)
                    .userId(userId)
                    .status(PtParticipantStatus.JOINED)
                    .joinedAt(Instant.now())
                    .build();
            ptRoomParticipantRepository.save(participant);
            return;
        }

        existing.join(); // LEFT였다면 재입장
        ptRoomParticipantRepository.save(existing);
    }

    @Transactional
    public void leaveRoom(Long ptRoomId, Long userId) {
        PtRoomParticipantEntity participant = ptRoomParticipantRepository.findByPtRoomIdAndUserId(ptRoomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        participant.exit();
        ptRoomParticipantRepository.save(participant);
    }

    @Transactional
    public void deleteRoom(Long ptRoomId, Long userId) {
        // 삭제도 방 row 잠그면 “삭제 중 join” 같은 경쟁에 더 강함
        PtRoomEntity room = ptRoomRepository.findByIdForUpdate(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!room.getTrainerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        room.cancel(); // CANCELLED + markDeleted()

        // JOINED → LEFT 정리
        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository.findAllByPtRoomId(ptRoomId);
        for (PtRoomParticipantEntity p : participants) {
            if (p.getStatus() == PtParticipantStatus.JOINED) {
                p.exit();
            }
        }

        // ✅ 키 반납 (멱등)
        ptJanusRoomKeyService.releaseByPtRoomId(ptRoomId);

        List<ReportEntity> pendingReports = reportRepository.findByTargetIdAndTypeAndStatus(
                ptRoomId,
                ReportType.PT_ROOM, // PT 방 타입
                ReportStatus.PENDING
        );

        for (ReportEntity report : pendingReports) {
            report.process(); // 신고 상태를 PROCESSED로 변경
        }
    }

    private String generateEntryCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    private PtRoomDetailResponseDTO assembleCreateResponse(
            PtRoomEntity room,
            UserEntity trainer,
            String entryCode,
            Integer janusKey,
            String bio
    ) {
        var trainerDTO = new PtRoomDetailResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle(), trainer.getProfileImageUrl(), bio);

        List<PtRoomDetailResponseDTO.UserDTO> participantsList =
                (room.getStatus() == PtRoomStatus.LIVE)
                        ? List.of(new PtRoomDetailResponseDTO.UserDTO(trainer.getNickname(), trainer.getHandle()))
                        : List.of();

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
                .janusRoomKey(janusKey == null ? null : String.valueOf(janusKey))
                .maxParticipants(room.getMaxParticipants())
                .participants(new PtRoomDetailResponseDTO.ParticipantsDTO(participantsList.size(), participantsList))
                .build();
    }
}