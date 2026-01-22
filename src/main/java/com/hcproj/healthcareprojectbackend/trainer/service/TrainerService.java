package com.hcproj.healthcareprojectbackend.trainer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.trainer.dto.request.TrainerApplicationRequestDTO;
import com.hcproj.healthcareprojectbackend.trainer.dto.response.TrainerApplicationResponseDTO;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerApplicationStatus;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerInfoRepository trainerInfoRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper; // Spring Boot 기본 Bean 주입

    @Transactional
    public TrainerApplicationResponseDTO submitApplication(Long userId, TrainerApplicationRequestDTO request) {
        // 1. 유저 권한 검증
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.TRAINER) {
            throw new BusinessException(ErrorCode.FORBIDDEN); // NOT_TRAINER
        }

        // 2. JSON 변환 (List<String> -> String)
        String licenseUrlsJson;
        try {
            licenseUrlsJson = objectMapper.writeValueAsString(request.licenseUrls());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 3. 기존 신청 내역 조회 (userId가 PK)
        TrainerInfoEntity trainerInfo = trainerInfoRepository.findById(userId).orElse(null);

        if (trainerInfo == null) {
            // [신규 신청]
            trainerInfo = TrainerInfoEntity.builder()
                    .userId(userId)
                    .bio(request.bio())
                    .licenseUrlsJson(licenseUrlsJson)
                    .applicationStatus(TrainerApplicationStatus.PENDING)
                    .build();
            trainerInfoRepository.save(trainerInfo);

        } else {
            // [기존 내역 존재]
            // 이미 승인되었거나(APPROVED), 심사 중(PENDING)이면 중복 신청 불가
            if (trainerInfo.getApplicationStatus() == TrainerApplicationStatus.APPROVED ||
                    trainerInfo.getApplicationStatus() == TrainerApplicationStatus.PENDING) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS); // ALREADY_SUBMITTED
            }

            // 거절됨(REJECTED) 상태라면 덮어쓰기(Overwrite) 허용 -> 재심사 요청
            trainerInfo.updateApplication(request.bio(), licenseUrlsJson);
        }

        return TrainerApplicationResponseDTO.of(trainerInfo.getApplicationStatus());
    }
}