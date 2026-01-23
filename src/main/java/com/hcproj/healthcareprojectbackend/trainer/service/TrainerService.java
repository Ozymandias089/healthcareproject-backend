package com.hcproj.healthcareprojectbackend.trainer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerInfoRepository trainerInfoRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // 1. 트레이너 자격증 제출 (신청)
    @Transactional
    public TrainerApplicationResponseDTO submitApplication(Long userId, TrainerApplicationRequestDTO request) {
        // 유저 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // JSON 변환
        String licenseUrlsJson;
        try {
            licenseUrlsJson = objectMapper.writeValueAsString(request.licenseUrls());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // Optional을 사용하여 재대입 문제 해결
        Optional<TrainerInfoEntity> optionalTrainerInfo = trainerInfoRepository.findById(userId);

        TrainerInfoEntity trainerInfo;

        if (optionalTrainerInfo.isEmpty()) {
            // [CASE 1] 최초 신청 (새로 생성)
            trainerInfo = TrainerInfoEntity.builder()
                    .userId(userId)
                    .bio(request.bio())
                    .licenseUrlsJson(licenseUrlsJson)
                    .applicationStatus(TrainerApplicationStatus.PENDING)
                    .build();
            trainerInfoRepository.save(trainerInfo);
        } else {
            // [CASE 2] 재신청 (기존 정보 수정)
            trainerInfo = optionalTrainerInfo.get();

            // 이미 승인되었거나 대기중이면 중복 신청 불가
            if (trainerInfo.getApplicationStatus() == TrainerApplicationStatus.APPROVED ||
                    trainerInfo.getApplicationStatus() == TrainerApplicationStatus.PENDING) {
                throw new BusinessException(ErrorCode.ALREADY_EXISTS);
            }
            // 거절된 상태(REJECTED)라면 다시 PENDING으로 갱신
            trainerInfo.updateApplication(request.bio(), licenseUrlsJson);
        }

        return TrainerApplicationResponseDTO.of(trainerInfo.getApplicationStatus());
    }

    // 2. 트레이너 소개문구 수정
    @Transactional
    public void updateBio(Long userId, String bio) {
        TrainerInfoEntity trainerInfo = trainerInfoRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        trainerInfo.updateBio(bio);
    }
}