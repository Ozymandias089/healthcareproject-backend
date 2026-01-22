package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomParticipantsResponseDTO; // ★ 추가됨
import com.hcproj.healthcareprojectbackend.pt.entity.*;
import com.hcproj.healthcareprojectbackend.pt.repository.PtReservationRepository;
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
    private final PtReservationRepository ptReservationRepository;
    private final UserRepository userRepository;

    /**
     * 화상PT 방 상세 조회
     */
    public PtRoomDetailResponseDTO getPtRoomDetail(Long ptRoomId) {
        PtRoomEntity ptRoom = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        UserEntity trainer = userRepository.findById(ptRoom.getTrainerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<PtRoomParticipantEntity> activeParticipants = ptRoomParticipantRepository.findAllByPtRoomId(ptRoomId)
                .stream()
                .filter(p -> p.getStatus() == PtParticipantStatus.JOINED) // 사람 상태는 JOINED
                .toList();

        List<Long> userIds = activeParticipants.stream().map(PtRoomParticipantEntity::getUserId).toList();
        List<PtRoomDetailResponseDTO.UserDTO> userDTOs = userRepository.findAllById(userIds).stream()
                .map(u -> new PtRoomDetailResponseDTO.UserDTO(u.getNickname(), u.getHandle()))
                .toList();

        return PtRoomDetailResponseDTO.builder()
                .ptRoomId(ptRoom.getPtRoomId())
                .title(ptRoom.getTitle())
                .description(ptRoom.getDescription())
                .scheduledAt(ptRoom.getScheduledStartAt())
                .trainer(new PtRoomDetailResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle(), null))
                .entryCode(null)
                .isPrivate(ptRoom.getIsPrivate())
                .roomType(ptRoom.getRoomType())
                .status(ptRoom.getStatus())
                .janusRoomKey(ptRoom.getJanusRoomKey())
                .maxParticipants(ptRoom.getMaxParticipants())
                .participants(new PtRoomDetailResponseDTO.ParticipantsDTO(userDTOs.size(), userDTOs))
                .build();
    }

    /*화상PT 참여 인원 조회 */
    public PtRoomParticipantsResponseDTO getPtRoomParticipants(Long ptRoomId, Long userId) {
        // 1. 방 존재 확인
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 권한 체크: 예약된 상태(REQUESTED)여야 함 (트레이너 포함)
         boolean isReserved = ptReservationRepository.existsByPtRoomIdAndUserIdAndStatus(
                ptRoomId, userId, PtReservationStatus.REQUESTED
         );

         if (!isReserved) {
//             명세서 요구사항: 예약자가 아니면 403 Forbidden
            throw new BusinessException(ErrorCode.FORBIDDEN);
         }

        // 3. 참여 중(JOINED)인 인원 조회 (입장 순)
        List<PtRoomParticipantEntity> participants = ptRoomParticipantRepository
                .findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(ptRoomId, PtParticipantStatus.JOINED);

        // 4. 유저 정보 조회 및 DTO 매핑
        List<Long> userIds = participants.stream().map(PtRoomParticipantEntity::getUserId).toList();
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<PtRoomParticipantsResponseDTO.UserDTO> userDTOs = participants.stream()
                .map(p -> {
                    UserEntity u = userMap.get(p.getUserId());
                    if (u == null) return null;

                    // 방장(트레이너)인지 확인하여 Role 설정
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

    /*화상PT 방 리스트 조회*/
    public PtRoomListResponseDTO getPtRoomList(String tab, String q, Long cursorId, int size, Long meId) {
        List<PtRoomStatus> statuses = null;
        Long trainerIdFilter = null;
        List<Long> roomIdFilter = null;

        switch (tab.toUpperCase()) {
            case "LIVE" -> statuses = List.of(PtRoomStatus.LIVE); // 방 상태는 LIVE
            case "RESERVED" -> statuses = List.of(PtRoomStatus.SCHEDULED);
            case "MY_RESERVATIONS" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                roomIdFilter = ptReservationRepository.findAllByUserId(meId).stream()
                        .filter(r -> r.getStatus() == PtReservationStatus.REQUESTED)
                        .map(PtReservationEntity::getPtRoomId).toList();
                if (roomIdFilter.isEmpty()) return emptyResponse(size);
            }
            case "MY_PT" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                trainerIdFilter = meId;
            }
            default -> statuses = List.of(PtRoomStatus.LIVE, PtRoomStatus.SCHEDULED);
        }

        List<PtRoomEntity> rooms = ptRoomRepository.findPtRoomsByFilters(cursorId, statuses, trainerIdFilter, roomIdFilter, PageRequest.of(0, size + 1));

        boolean hasNext = rooms.size() > size;
        if (hasNext) rooms.remove(size);
        Long nextCursorId = rooms.isEmpty() ? null : rooms.get(rooms.size() - 1).getPtRoomId();

        return assembleListResponse(rooms, nextCursorId, hasNext, size);
    }

    private PtRoomListResponseDTO assembleListResponse(List<PtRoomEntity> rooms, Long nextCursorId, boolean hasNext, int size) {
        Set<Long> trainerIds = rooms.stream().map(PtRoomEntity::getTrainerId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(trainerIds).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<PtRoomListResponseDTO.ItemDTO> items = rooms.stream().map(r -> {
            UserEntity trainer = userMap.get(r.getTrainerId());
            int currentCount = ptRoomParticipantRepository.findAllByPtRoomId(r.getPtRoomId()).size();

            return PtRoomListResponseDTO.ItemDTO.builder()
                    .ptRoomId(r.getPtRoomId()).title(r.getTitle()).roomType(r.getRoomType())
                    .status(r.getStatus()).scheduledAt(r.getScheduledStartAt())
                    .isPrivate(r.getIsPrivate())
                    .trainer(new PtRoomListResponseDTO.TrainerDTO(
                            trainer != null ? trainer.getNickname() : "알수없음",
                            trainer != null ? trainer.getHandle() : "unknown", null))
                    .participants(new PtRoomListResponseDTO.ParticipantsDTO(currentCount, r.getMaxParticipants()))
                    .build();
        }).toList();

        return new PtRoomListResponseDTO(items, new PtRoomListResponseDTO.PageInfo(nextCursorId, hasNext, size));
    }

    private PtRoomListResponseDTO emptyResponse(int size) {
        return new PtRoomListResponseDTO(List.of(), new PtRoomListResponseDTO.PageInfo(null, false, size));
    }
}