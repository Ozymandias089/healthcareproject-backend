package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomParticipantsResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtJanusRoomKeyRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomParticipantRepository;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PtRoomQueryService {

    private final PtRoomRepository ptRoomRepository;
    private final PtRoomParticipantRepository ptRoomParticipantRepository;
    private final PtJanusRoomKeyRepository ptJanusRoomKeyRepository;
    private final UserRepository userRepository;

    public PtRoomDetailResponseDTO getPtRoomDetail(Long ptRoomId, Long currentUserId) {
        PtRoomEntity ptRoom = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        UserEntity trainer = userRepository.findById(ptRoom.getTrainerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // JOINED 참가자만
        List<PtRoomParticipantEntity> activeParticipants = ptRoomParticipantRepository
                .findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(ptRoomId, PtParticipantStatus.JOINED);

        List<Long> userIds = activeParticipants.stream().map(PtRoomParticipantEntity::getUserId).toList();
        List<PtRoomDetailResponseDTO.UserDTO> userDTOs = userRepository.findAllById(userIds).stream()
                .map(u -> new PtRoomDetailResponseDTO.UserDTO(u.getNickname(), u.getHandle()))
                .toList();

        // 트레이너 본인일 때만 entryCode 노출
        String entryCode = null;
        if (currentUserId != null && currentUserId.equals(ptRoom.getTrainerId())) {
            entryCode = ptRoom.getEntryCode();
        }

        // janus 키 조회
        String janusRoomKey = ptJanusRoomKeyRepository.findByPtRoomId(ptRoomId)
                .map(k -> String.valueOf(k.getRoomKey()))
                .orElse(null);


        return PtRoomDetailResponseDTO.builder()
                .ptRoomId(ptRoom.getPtRoomId())
                .title(ptRoom.getTitle())
                .description(ptRoom.getDescription())
                .scheduledAt(ptRoom.getScheduledStartAt())
                .trainer(new PtRoomDetailResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle(), null))
                .entryCode(entryCode)
                .isPrivate(ptRoom.getIsPrivate())
                .roomType(ptRoom.getRoomType())
                .status(ptRoom.getStatus())
                .janusRoomKey(janusRoomKey)
                .maxParticipants(ptRoom.getMaxParticipants())
                .participants(new PtRoomDetailResponseDTO.ParticipantsDTO(userDTOs.size(), userDTOs))
                .build();
    }

    public PtRoomParticipantsResponseDTO getPtRoomParticipants(Long ptRoomId, Long userId) {
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // ✅ 권한: 트레이너 OR 현재 JOINED인 유저만
        boolean isTrainer = room.getTrainerId().equals(userId);
        boolean isJoined = ptRoomParticipantRepository.existsByPtRoomIdAndUserIdAndStatus(ptRoomId, userId, PtParticipantStatus.JOINED);

        if (!isTrainer && !isJoined) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository
                .findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(ptRoomId, PtParticipantStatus.JOINED);

        List<Long> userIds = participants.stream().map(PtRoomParticipantEntity::getUserId).toList();
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<PtRoomParticipantsResponseDTO.UserDTO> userDTOs = participants.stream()
                .map(p -> {
                    UserEntity u = userMap.get(p.getUserId());
                    if (u == null) return null;

                    String role = room.getTrainerId().equals(u.getId()) ? "TRAINER" : "USER";

                    return PtRoomParticipantsResponseDTO.UserDTO.builder()
                            .handle(u.getHandle())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .role(role)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return PtRoomParticipantsResponseDTO.builder()
                .ptRoomId(room.getPtRoomId())
                .count(userDTOs.size())
                .users(userDTOs)
                .build();
    }

    public PtRoomListResponseDTO getPtRoomList(String tab, String q, Long cursorId, int size, Long meId) {
        List<PtRoomStatus> statuses = null;
        Long trainerIdFilter = null;
        List<Long> roomIdFilter = null;

        switch (tab.toUpperCase()) {
            case "LIVE" -> statuses = List.of(PtRoomStatus.LIVE);
            case "RESERVED" -> statuses = List.of(PtRoomStatus.SCHEDULED);
            case "MY_JOINED" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                roomIdFilter = ptRoomParticipantRepository.findAllByUserId(meId).stream()
                        .filter(p -> p.getStatus() == PtParticipantStatus.JOINED || p.getStatus() == PtParticipantStatus.LEFT)
                        .map(PtRoomParticipantEntity::getPtRoomId)
                        .distinct()
                        .toList();
                if (roomIdFilter.isEmpty()) return emptyResponse(size);
            }
            case "MY_PT" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                trainerIdFilter = meId;
            }
            default -> statuses = List.of(PtRoomStatus.LIVE, PtRoomStatus.SCHEDULED);
        }

        // q 검색은 아직 미적용(원하면 title like 조건 추가)
        List<PtRoomEntity> rooms = ptRoomRepository.findPtRoomsByFilters(
                cursorId, statuses, trainerIdFilter, roomIdFilter, PageRequest.of(0, size + 1)
        );

        boolean hasNext = rooms.size() > size;
        if (hasNext) rooms.remove(size);
        Long nextCursorId = rooms.isEmpty() ? null : rooms.getLast().getPtRoomId();

        return assembleListResponse(rooms, nextCursorId, hasNext, size);
    }

    private PtRoomListResponseDTO assembleListResponse(List<PtRoomEntity> rooms, Long nextCursorId, boolean hasNext, int size) {
        Set<Long> trainerIds = rooms.stream().map(PtRoomEntity::getTrainerId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(trainerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // janus 키를 배치로 가져오고 싶으면 roomIds로 한 번에 조회하는 메서드 추가 추천
        List<PtRoomListResponseDTO.ItemDTO> items = rooms.stream().map(r -> {
            UserEntity trainer = userMap.get(r.getTrainerId());

            int joinedCount = (int) ptRoomParticipantRepository.countByPtRoomIdAndStatus(r.getPtRoomId(), PtParticipantStatus.JOINED);

            return PtRoomListResponseDTO.ItemDTO.builder()
                    .ptRoomId(r.getPtRoomId())
                    .title(r.getTitle())
                    .roomType(r.getRoomType())
                    .status(r.getStatus())
                    .scheduledAt(r.getScheduledStartAt())
                    .isPrivate(r.getIsPrivate())
                    .trainer(new PtRoomListResponseDTO.TrainerDTO(
                            trainer != null ? trainer.getNickname() : "알수없음",
                            trainer != null ? trainer.getHandle() : "unknown",
                            null
                    ))
                    .participants(new PtRoomListResponseDTO.ParticipantsDTO(joinedCount, r.getMaxParticipants()))
                    .build();
        }).toList();

        return new PtRoomListResponseDTO(items, new PtRoomListResponseDTO.PageInfo(nextCursorId, hasNext, size));
    }

    private PtRoomListResponseDTO emptyResponse(int size) {
        return new PtRoomListResponseDTO(List.of(), new PtRoomListResponseDTO.PageInfo(null, false, size));
    }
}
