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
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.hcproj.healthcareprojectbackend.global.util.UtilityProvider.normalizeKeyword;

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
        PtRoomEntity ptRoom = getRoomOrThrow(ptRoomId);
        UserRepository.TrainerProfileView trainerProfile = loadTrainerProfile(ptRoom.getTrainerId());

        // JOINED 참가자만
        List<PtRoomParticipantEntity> activeParticipants = ptRoomParticipantRepository
                .findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(ptRoomId, PtParticipantStatus.JOINED);

        List<PtRoomDetailResponseDTO.UserDTO> userDTOs = toDetailUsers(activeParticipants);

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
                .trainer(new PtRoomDetailResponseDTO.TrainerDTO(
                        trainerProfile.getNickname(),
                        trainerProfile.getHandle(),
                        trainerProfile.getProfileImageUrl(),
                        trainerProfile.getBio()
                ))
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
        PtRoomEntity room = getRoomOrThrow(ptRoomId);
        validateParticipantAccess(room, userId);

        List<PtRoomParticipantEntity> participants = findJoinedParticipants(ptRoomId);
        Map<Long, UserEntity> userMap = loadUsersByIds(
                participants.stream().map(PtRoomParticipantEntity::getUserId).toList()
        );
        List<PtRoomParticipantsResponseDTO.UserDTO> userDTOs = toParticipantUsers(
                participants, userMap, room.getTrainerId()
        );

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

    private List<Long> getJoinedRoomIds(Long userId) {
        return ptRoomParticipantRepository.findAllByUserId(userId).stream()
                .filter(p -> p.getStatus() == PtParticipantStatus.JOINED || p.getStatus() == PtParticipantStatus.LEFT)
                .map(PtRoomParticipantEntity::getPtRoomId)
                .distinct()
                .toList();
    }

    private PtRoomListResponseDTO assembleListResponse(List<PtRoomEntity> rooms, Long nextCursorId, boolean hasNext, int size) {
        Map<Long, UserEntity> userMap = loadUsersByIds(
                rooms.stream().map(PtRoomEntity::getTrainerId).distinct().toList()
        );
        Map<Long, TrainerInfoEntity> trainerMap = loadTrainerInfosByIds(
                rooms.stream().map(PtRoomEntity::getTrainerId).distinct().toList()
        );
        Map<Long, Long> joinedCountMap = loadJoinedCountMap(
                rooms.stream().map(PtRoomEntity::getPtRoomId).toList()
        );

        List<PtRoomListResponseDTO.ItemDTO> items = rooms.stream()
                .map(r -> toListItem(r, userMap, trainerMap, joinedCountMap))
                .toList();

        return new PtRoomListResponseDTO(items, new PtRoomListResponseDTO.PageInfo(nextCursorId, hasNext, size));
    }

    private PtRoomListResponseDTO emptyResponse(int size) {
        return new PtRoomListResponseDTO(List.of(), new PtRoomListResponseDTO.PageInfo(null, false, size));
    }

    private PtRoomEntity getRoomOrThrow(Long ptRoomId) {
        return ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private UserRepository.TrainerProfileView loadTrainerProfile(Long trainerId) {
        return userRepository.findTrainerProfileById(trainerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateParticipantAccess(PtRoomEntity room, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        boolean isTrainer = room.getTrainerId().equals(userId);
        boolean isJoined = ptRoomParticipantRepository.existsByPtRoomIdAndUserIdAndStatus(
                room.getPtRoomId(), userId, PtParticipantStatus.JOINED
        );
        if (!isTrainer && !isJoined) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private List<PtRoomParticipantEntity> findJoinedParticipants(Long ptRoomId) {
        return ptRoomParticipantRepository
                .findAllByPtRoomIdAndStatusOrderByJoinedAtAsc(ptRoomId, PtParticipantStatus.JOINED);
    }

    private Map<Long, UserEntity> loadUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
    }

    private Map<Long, TrainerInfoEntity> loadTrainerInfosByIds(List<Long> trainerIds) {
        if (trainerIds == null || trainerIds.isEmpty()) {
            return Map.of();
        }
        return trainerInfoRepository.findAllById(trainerIds).stream()
                .collect(Collectors.toMap(TrainerInfoEntity::getUserId, u -> u));
    }

    private Map<Long, Long> loadJoinedCountMap(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return Map.of();
        }
        return ptRoomParticipantRepository.countByPtRoomIdsAndStatus(roomIds, PtParticipantStatus.JOINED).stream()
                .collect(Collectors.toMap(
                        PtRoomParticipantRepository.RoomCount::getPtRoomId,
                        PtRoomParticipantRepository.RoomCount::getCount
                ));
    }

    private List<PtRoomDetailResponseDTO.UserDTO> toDetailUsers(List<PtRoomParticipantEntity> participants) {
        Map<Long, UserEntity> userMap = loadUsersByIds(
                participants.stream().map(PtRoomParticipantEntity::getUserId).toList()
        );
        return participants.stream()
                .map(p -> userMap.get(p.getUserId()))
                .filter(Objects::nonNull)
                .map(u -> new PtRoomDetailResponseDTO.UserDTO(u.getNickname(), u.getHandle()))
                .toList();
    }

    private List<PtRoomParticipantsResponseDTO.UserDTO> toParticipantUsers(
            List<PtRoomParticipantEntity> participants,
            Map<Long, UserEntity> userMap,
            Long trainerId
    ) {
        return participants.stream()
                .map(p -> {
                    UserEntity u = userMap.get(p.getUserId());
                    if (u == null) return null;

                    String role = trainerId.equals(u.getId()) ? "TRAINER" : "USER";
                    return PtRoomParticipantsResponseDTO.UserDTO.builder()
                            .handle(u.getHandle())
                            .nickname(u.getNickname())
                            .profileImageUrl(u.getProfileImageUrl())
                            .role(role)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private PtRoomListResponseDTO.ItemDTO toListItem(
            PtRoomEntity room,
            Map<Long, UserEntity> userMap,
            Map<Long, TrainerInfoEntity> trainerMap,
            Map<Long, Long> joinedCountMap
    ) {
        UserEntity trainer = userMap.get(room.getTrainerId());
        TrainerInfoEntity trainerInfo = trainerMap.get(room.getTrainerId());
        int joinedCount = joinedCountMap.getOrDefault(room.getPtRoomId(), 0L).intValue();

        return PtRoomListResponseDTO.ItemDTO.builder()
                .ptRoomId(room.getPtRoomId())
                .title(room.getTitle())
                .roomType(room.getRoomType())
                .status(room.getStatus())
                .scheduledAt(room.getScheduledStartAt())
                .isPrivate(room.getIsPrivate())
                .trainer(new PtRoomListResponseDTO.TrainerDTO(
                        trainer != null ? trainer.getNickname() : "알수없음",
                        trainer != null ? trainer.getHandle() : "unknown",
                        trainer != null ? trainer.getProfileImageUrl() : null,
                        trainer != null && trainerInfo != null ? trainerInfo.getBio() : null
                ))
                .participants(new PtRoomListResponseDTO.ParticipantsDTO(joinedCount, room.getMaxParticipants()))
                .build();
    }
}
