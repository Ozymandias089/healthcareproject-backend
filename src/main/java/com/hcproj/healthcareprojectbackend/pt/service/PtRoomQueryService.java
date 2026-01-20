package com.hcproj.healthcareprojectbackend.pt.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
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
        // 1. 방 정보 조회 (404 예외 처리)
        PtRoomEntity ptRoom = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2. 트레이너 정보 조회
        UserEntity trainer = userRepository.findById(ptRoom.getTrainerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 현재 참여 중인 유저 목록 조회 (status = LIVE 기준)
        List<PtRoomParticipantEntity> activeParticipants = ptRoomParticipantRepository.findAllByPtRoomId(ptRoomId)
                .stream()
                .filter(p -> p.getStatus() == PtParticipantStatus.LIVE)
                .toList();

        // 4. 참여자 상세 정보(닉네임, 핸들) 매핑
        List<Long> userIds = activeParticipants.stream().map(PtRoomParticipantEntity::getUserId).toList();
        List<PtRoomDetailResponseDTO.UserDTO> userDTOs = userRepository.findAllById(userIds).stream()
                .map(u -> new PtRoomDetailResponseDTO.UserDTO(u.getNickname(), u.getHandle()))
                .toList();

        // 5. 응답 조립 (entryCode는 보안상 null 반환)
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

    /**
     * 화상PT 방 리스트 조회 (무한 스크롤 + 검색)
     */
    public PtRoomListResponseDTO getPtRoomList(String tab, String q, Long cursorId, int size, Long meId) {
        List<PtRoomStatus> statuses = null;
        Long trainerIdFilter = null;
        List<Long> roomIdFilter = null;

        switch (tab.toUpperCase()) {
            case "LIVE" -> statuses = List.of(PtRoomStatus.LIVE);
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

        if (q != null && !q.isBlank()) {
            rooms = filterBySearchQuery(rooms, q);
        }

        boolean hasNext = rooms.size() > size;
        if (hasNext) rooms.remove(size);
        Long nextCursorId = rooms.isEmpty() ? null : rooms.get(rooms.size() - 1).getPtRoomId();

        return assembleListResponse(rooms, nextCursorId, hasNext, size);
    }

    private List<PtRoomEntity> filterBySearchQuery(List<PtRoomEntity> rooms, String q) {
        Set<Long> trainerIds = rooms.stream().map(PtRoomEntity::getTrainerId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(trainerIds).stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

        return rooms.stream().filter(r -> {
            UserEntity trainer = userMap.get(r.getTrainerId());
            return r.getTitle().contains(q) || (trainer != null && (trainer.getNickname().contains(q) || trainer.getHandle().contains(q)));
        }).collect(Collectors.toList());
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