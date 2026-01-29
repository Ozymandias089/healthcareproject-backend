package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminPtRoomForceCloseRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPtRoomForceCloseResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.entity.ReportEntity;
import com.hcproj.healthcareprojectbackend.community.entity.ReportStatus;
import com.hcproj.healthcareprojectbackend.community.entity.ReportType;
import com.hcproj.healthcareprojectbackend.community.repository.ReportRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.util.UtilityProvider;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomEntity;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.repository.PtRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 화상 PT 방 서비스
 */
@Service
@RequiredArgsConstructor
public class AdminPtRoomService {

    private final PtRoomRepository ptRoomRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    /**
     * 관리자 화상 PT 방 목록 조회
     *
     * @param status        방 상태 필터 (SCHEDULED, LIVE, ENDED, CANCELLED, FORCE_CLOSED)
     * @param trainerHandle 트레이너 핸들 필터
     * @param page          페이지 번호
     * @param size          한 페이지당 개수
     * @param query         검색어
     * @return PT 방 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public AdminPtRoomListResponseDTO getAdminPtRoomList(
            String status,
            String trainerHandle,
            int page,
            int size,
            String query
    ) {
        // 1) 상태 필터 변환
        String statusParam = null;
        if (status != null && !status.isBlank()) {
            try {
                // 유효한 상태인지 확인
                PtRoomStatus.valueOf(status.toUpperCase());
                statusParam = status.toUpperCase();
            } catch (IllegalArgumentException e) {
                // 잘못된 status 값은 무시 (전체 조회)
                statusParam = null;
            }
        }

        // 2) 트레이너 핸들로 trainerId 조회
        Long trainerId = null;
        if (trainerHandle != null && !trainerHandle.isBlank()) {
            trainerId = userRepository.findByHandle(trainerHandle)
                    .map(UserEntity::getId)
                    .orElse(null);
            // 트레이너를 찾지 못하면 빈 결과 반환
            if (trainerId == null) {
                return AdminPtRoomListResponseDTO.builder()
                        .total(0)
                        .list(Collections.emptyList())
                        .build();
            }
        }

        // 3) 검색어 정규화
        String normalizedKeyword = UtilityProvider.normalizeKeyword(query);

        // 4) 페이지네이션 설정
        int offsetSize = page * size;

        // 5) 조회 (검색어 유무에 따라 분기)
        List<PtRoomEntity> rooms;
        long total;

        if (normalizedKeyword == null) {
            // 검색어 없음
            rooms = ptRoomRepository.findAdminPtRoomsNoKeyword(
                    statusParam, trainerId, size, offsetSize
            );
            total = ptRoomRepository.countAdminPtRoomsNoKeyword(statusParam, trainerId);
        } else {
            // 검색어 있음 - 띄어쓰기 제거 + 소문자 변환 + 와일드카드 추가
            String likePattern = "%" + normalizedKeyword.toLowerCase().replace(" ", "") + "%";
            rooms = ptRoomRepository.findAdminPtRoomsWithKeyword(
                    statusParam, trainerId, likePattern, size, offsetSize
            );
            total = ptRoomRepository.countAdminPtRoomsWithKeyword(statusParam, trainerId, likePattern);
        }

        // 6) 트레이너 정보 조회
        List<Long> trainerIds = rooms.stream()
                .map(PtRoomEntity::getTrainerId)
                .distinct()
                .toList();

        Map<Long, UserEntity> trainerMap = trainerIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(trainerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // 7) 응답 DTO 변환
        List<AdminPtRoomListResponseDTO.AdminPtRoomItemDTO> list = rooms.stream()
                .map(room -> {
                    UserEntity trainer = trainerMap.get(room.getTrainerId());
                    return AdminPtRoomListResponseDTO.AdminPtRoomItemDTO.builder()
                            .ptRoomId(room.getPtRoomId())
                            .trainer(AdminPtRoomListResponseDTO.TrainerDTO.builder()
                                    .nickname(trainer != null ? trainer.getNickname() : "알 수 없음")
                                    .handle(trainer != null ? trainer.getHandle() : "unknown")
                                    .build())
                            .title(room.getTitle())
                            .description(room.getDescription())
                            .roomType(room.getRoomType())
                            .scheduledStartAt(room.getScheduledStartAt())
                            .maxParticipants(room.getMaxParticipants())
                            .isPrivate(room.getIsPrivate())
                            .entryCode(room.getEntryCode())
                            .status(room.getStatus())
                            .createdAt(room.getCreatedAt())
                            .build();
                })
                .toList();

        return AdminPtRoomListResponseDTO.builder()
                .total(total)
                .list(list)
                .build();
    }

    /**
     * 관리자 화상 PT 방 강제 종료
     *
     * @param ptRoomId 종료할 PT 방 ID
     * @param request  강제 종료 요청 DTO
     * @return 강제 종료 응답 DTO
     */
    @Transactional
    public AdminPtRoomForceCloseResponseDTO forceClosePtRoom(
            Long ptRoomId,
            AdminPtRoomForceCloseRequestDTO request
    ) {
        // 1) PT 방 조회
        PtRoomEntity room = ptRoomRepository.findById(ptRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 2) 이미 종료된 상태인지 확인
        if (room.getStatus() == PtRoomStatus.ENDED ||
                room.getStatus() == PtRoomStatus.CANCELLED ||
                room.getStatus() == PtRoomStatus.FORCE_CLOSED) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 3) 강제 종료 처리
        room.forceClose();

        List<ReportEntity> pendingReports = reportRepository.findByTargetIdAndTypeAndStatus(
                ptRoomId,
                ReportType.PT_ROOM,
                ReportStatus.PENDING
        );

        for (ReportEntity report : pendingReports) {
            report.process();
        }

        // 4) 저장
        PtRoomEntity savedRoom = ptRoomRepository.save(room);

        // 5) 응답 DTO 반환
        return AdminPtRoomForceCloseResponseDTO.builder()
                .ptRoomId(savedRoom.getPtRoomId())
                .status(savedRoom.getStatus())
                .message("관리자에 의해 중지된 화상 PT입니다.")
                .closedAt(savedRoom.getDeletedAt())
                .build();
    }

}