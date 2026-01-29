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
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
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
    private final TrainerInfoRepository trainerInfoRepository;

    public PtRoomDetailResponseDTO getPtRoomDetail(Long ptRoomId, Long currentUserId) {
        PtRoomEntity ptRoom = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        UserEntity trainer = userRepository.findById(ptRoom.getTrainerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String bio = trainerInfoRepository.findBioByTrainerId(trainer.getId()).orElse(null);

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
                .trainer(new PtRoomDetailResponseDTO.TrainerDTO(trainer.getNickname(), trainer.getHandle(), trainer.getProfileImageUrl(), bio))
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

        // 권한: 트레이너 OR 현재 JOINED인 유저만
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

    /**
     * PT 방 목록 조회 (커서 기반 페이지네이션 + 검색 + 띄어쓰기 무시)
     */
    public PtRoomListResponseDTO getPtRoomList(String tab, String q, Long cursorId, int size, Long meId) {
        int limitSize = size + 1;
        List<PtRoomEntity> rooms;

        // 1. 검색어 정규화
        String keyword = normalizeKeyword(q);

        // 2. 탭에 따른 분기
        String normalizedTab = (tab == null) ? "ALL" : tab.toUpperCase();

        if (keyword == null) {
            // 검색어 없음 → 기존 JPQL 사용
            rooms = findRoomsWithoutSearch(normalizedTab, cursorId, limitSize, meId);
        } else {
            // 검색어 있음 → Native Query 사용 (띄어쓰기 무시)
            String likePattern = "%" + keyword.toLowerCase().replace(" ", "") + "%";
            rooms = findRoomsWithSearch(normalizedTab, cursorId, likePattern, limitSize, meId);
        }

        // 3. 빈 결과 처리
        if (rooms == null || rooms.isEmpty()) {
            return emptyResponse(size);
        }

        // 4. hasNext 판단
        boolean hasNext = rooms.size() > size;
        if (hasNext) {
            rooms = new ArrayList<>(rooms.subList(0, size));
        }

        Long nextCursorId = rooms.isEmpty() ? null : rooms.get(rooms.size() - 1).getPtRoomId();

        return assembleListResponse(rooms, nextCursorId, hasNext, size);
    }

    // ============================================================
    // 검색어 없을 때 (기존 JPQL)
    // ============================================================
    private List<PtRoomEntity> findRoomsWithoutSearch(String tab, Long cursorId, int limitSize, Long meId) {
        List<PtRoomStatus> statuses = null;
        Long trainerIdFilter = null;
        List<Long> roomIdFilter = null;

        switch (tab) {
            case "LIVE" -> statuses = List.of(PtRoomStatus.LIVE);
            case "RESERVED" -> statuses = List.of(PtRoomStatus.SCHEDULED);
            case "MY_JOINED" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                roomIdFilter = getJoinedRoomIds(meId);
                if (roomIdFilter.isEmpty()) return List.of();
            }
            case "MY_PT" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                trainerIdFilter = meId;
            }
            default -> statuses = List.of(PtRoomStatus.LIVE, PtRoomStatus.SCHEDULED);
        }

        return ptRoomRepository.findPtRoomsByFilters(
                cursorId, statuses, trainerIdFilter, roomIdFilter, null, PageRequest.of(0, limitSize)
        );
    }

    // ============================================================
    // 검색어 있을 때 (Native Query - 띄어쓰기 무시)
    // ============================================================
    private List<PtRoomEntity> findRoomsWithSearch(String tab, Long cursorId, String likePattern, int limitSize, Long meId) {
        switch (tab) {
            case "LIVE" -> {
                return ptRoomRepository.findPtRoomsLiveWithSearch(cursorId, likePattern, limitSize);
            }
            case "RESERVED" -> {
                return ptRoomRepository.findPtRoomsScheduledWithSearch(cursorId, likePattern, limitSize);
            }
            case "MY_JOINED" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                List<Long> roomIds = getJoinedRoomIds(meId);
                if (roomIds.isEmpty()) return List.of();
                return ptRoomRepository.findPtRoomsByRoomIdsWithSearch(cursorId, roomIds, likePattern, limitSize);
            }
            case "MY_PT" -> {
                if (meId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
                return ptRoomRepository.findPtRoomsByTrainerWithSearch(cursorId, meId, likePattern, limitSize);
            }
            default -> {
                // ALL 탭
                return ptRoomRepository.findPtRoomsAllWithSearch(cursorId, likePattern, limitSize);
            }
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private String normalizeKeyword(String q) {
        if (q == null) return null;
        String trimmed = q.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private List<Long> getJoinedRoomIds(Long userId) {
        return ptRoomParticipantRepository.findAllByUserId(userId).stream()
                .filter(p -> p.getStatus() == PtParticipantStatus.JOINED || p.getStatus() == PtParticipantStatus.LEFT)
                .map(PtRoomParticipantEntity::getPtRoomId)
                .distinct()
                .toList();
    }

    private PtRoomListResponseDTO assembleListResponse(List<PtRoomEntity> rooms, Long nextCursorId, boolean hasNext, int size) {
        Set<Long> trainerIds = rooms.stream().map(PtRoomEntity::getTrainerId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(trainerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

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
                            trainer != null ? trainer.getProfileImageUrl() : null
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