package com.hcproj.healthcareprojectbackend.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.admin.dto.response.TrainerPendingListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.TrainerRejectResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminUserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.trainer.dto.response.TrainerApproveResponseDTO;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTrainerService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final TrainerInfoRepository trainerInfoRepository;
    private final ObjectMapper objectMapper; // [추가] JSON 변환을 위해 필요

    // 1. 관리자: 트레이너 승인
    @Transactional
    public TrainerApproveResponseDTO approveTrainer(String handle) {
        UserEntity user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        TrainerInfoEntity trainerInfo = trainerInfoRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        trainerInfo.approve();
        adminUserRepository.updateUserRole(handle, UserRole.TRAINER);

        return TrainerApproveResponseDTO.of(
                user.getHandle(),
                UserRole.TRAINER.name(),
                trainerInfo.getApprovedAt()
        );
    }

    // 2. 관리자: 승인 대기자 목록 조회
    @Transactional(readOnly = true)
    public TrainerPendingListResponseDTO getPendingTrainerList(Pageable pageable) {
        Page<TrainerInfoEntity> pendingPage = trainerInfoRepository.findAllByApplicationStatus(
                TrainerApplicationStatus.PENDING,
                pageable
        );

        List<Long> userIds = pendingPage.stream()
                .map(TrainerInfoEntity::getUserId)
                .toList();

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        List<TrainerPendingListResponseDTO.TrainerApplicantDTO> applicantList = pendingPage.stream()
                .map(info -> {
                    UserEntity user = userMap.get(info.getUserId());
                    if (user == null) return null;

                    // JSON String -> List<String> 변환 로직
                    List<String> licenseUrls = new ArrayList<>();
                    if (info.getLicenseUrlsJson() != null && !info.getLicenseUrlsJson().isBlank()) {
                        try {
                            licenseUrls = objectMapper.readValue(info.getLicenseUrlsJson(), new TypeReference<>() {});
                        } catch (JsonProcessingException e) {
                            // JSON 파싱 실패 시 빈 리스트 반환 (혹은 로그 출력)
                        }
                    }

                    return new TrainerPendingListResponseDTO.TrainerApplicantDTO(
                            user.getHandle(),
                            user.getNickname(),
                            user.getProfileImageUrl(),
                            licenseUrls, // 변환된 리스트 사용
                            info.getBio(),
                            info.getCreatedAt()
                    );
                })
                .filter(dto -> dto != null)
                .toList();

        return TrainerPendingListResponseDTO.builder()
                .applicant(applicantList)
                .page(pendingPage.getNumber())
                .size(pendingPage.getSize())
                .totalElements(pendingPage.getTotalElements())
                .hasPrev(pendingPage.hasPrevious())
                .hasNext(pendingPage.hasNext())
                .build();
    }

    // [추가됨] 관리자: 트레이너 신청 거절
    @Transactional
    public TrainerRejectResponseDTO rejectTrainer(String handle, String reason) {
        // 1. 유저 조회
        UserEntity user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 신청 내역 조회
        TrainerInfoEntity trainerInfo = trainerInfoRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 3. 거절 처리 (Entity 메서드 호출)
        trainerInfo.reject(reason);

        // 4. 응답 반환 (거절 시각은 현재 시각)
        return TrainerRejectResponseDTO.of(
                user.getHandle(),
                trainerInfo.getApplicationStatus().name(),
                trainerInfo.getRejectReason(),
                Instant.now()
        );
    }
}