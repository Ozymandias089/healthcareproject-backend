package com.hcproj.healthcareprojectbackend.me.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.security.jwt.TokenVersionStore;
import com.hcproj.healthcareprojectbackend.me.dto.internal.InjuriesRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.internal.ProfileDTO;
import com.hcproj.healthcareprojectbackend.me.dto.request.*;
import com.hcproj.healthcareprojectbackend.me.dto.response.MeResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.TrainerInfoResponseDTO;
import com.hcproj.healthcareprojectbackend.profile.entity.AllergyType;
import com.hcproj.healthcareprojectbackend.profile.entity.InjuryLevel;
import com.hcproj.healthcareprojectbackend.profile.entity.UserInjuryEntity;
import com.hcproj.healthcareprojectbackend.profile.entity.UserProfileEntity;
import com.hcproj.healthcareprojectbackend.profile.repository.UserInjuryRepository;
import com.hcproj.healthcareprojectbackend.profile.repository.UserProfileRepository;
import com.hcproj.healthcareprojectbackend.trainer.entity.TrainerInfoEntity;
import com.hcproj.healthcareprojectbackend.trainer.repository.TrainerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * /api/me 계열 유스케이스 서비스.
 * <p>
 * auth/profile 등 도메인 모듈을 "마이페이지 관점"에서 조합하는 Facade 역할을 수행할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class MeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenVersionStore tokenVersionStore;
    private final UserProfileRepository userProfileRepository;
    private final UserInjuryRepository userInjuryRepository;
    private final TrainerInfoRepository trainerInfoRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public MeResponseDTO getMe(Long userId) {
        UserEntity user = getUserOrThrow(userId);
        return toMeResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDTO request) {
        UserEntity user = getUserOrThrow(userId);

        // 회원이 활성화가 아닌 경우 변경불가
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
        // JPA dirty checking으로 저장됨
    }

    // 회원탈퇴
    @Transactional
    public void withdraw(Long userId) {
        UserEntity user = getUserOrThrow(userId);

        // 이미 탈퇴된 회원의 경우 409 CONFLICT 반환
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }

        user.withdraw();

        tokenVersionStore.bump(userId);
    }

    @Transactional
    public void onboarding(Long userId, OnboardingRequestDTO request) {
        /// 유저 정보 존재여부 검증
        if (!userRepository.existsById(userId)) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        /// 기존에 유저의 부상정보가 있다면 드랍
        userInjuryRepository.deleteByUserId(userId);

        /// requestDTO에서 프로필, 부상 정보 추출
        ProfileDTO profileDTO = request.profile();
        List<InjuriesRequestDTO> injuriesRequestDTOs = request.injuries();

        /// 알러지 정보 가공
        List<AllergyType> allergies = request.allergies().stream()
                .map(AllergyType::from)
                .toList();

        /// 프로필 객체 생성
        UserProfileEntity profile = UserProfileEntity.builder()
                .userId(userId)
                .heightCm(profileDTO.heightCm())
                .weightKg(profileDTO.weightKg())
                .age(profileDTO.age())
                .gender(profileDTO.gender())
                .experienceLevel(profileDTO.experienceLevel())
                .goalType(profileDTO.goalType())
                .weeklyDays(profileDTO.weeklyDays())
                .sessionMinutes(profileDTO.sessionMinutes())
                .allergies(allergies)
                .build();

        /// 부상정보 가공 및 매핑
        List<UserInjuryEntity> injuryEntities = injuriesRequestDTOs.stream()
                .map(dto ->
                    UserInjuryEntity.builder()
                        .userId(userId)
                        .injuryPart(dto.injuryPart())
                        .injuryLevel(InjuryLevel.from(dto.injuryLevel()))
                        .build()
                ).toList();

        ///  프로필 정보 저장
        userProfileRepository.save(profile);

        ///  부상정보 저장
        if (!injuryEntities.isEmpty()) userInjuryRepository.saveAll(injuryEntities);
    }

    @Transactional
    public MeResponseDTO changeNickname(Long userId, ChangeNicknameRequestDTO request) {
        UserEntity user = getUserOrThrow(userId);
        user.changeNickname(request.nickname());
        return toMeResponse(user);
    }

    @Transactional
    public MeResponseDTO changePhoneNumber(Long userId, ChangePhoneNumberRequestDTO request) {
        UserEntity user = getUserOrThrow(userId);
        user.changePhoneNumber(request.phoneNumber());
        return toMeResponse(user);
    }

    @Transactional
    public MeResponseDTO changeProfileImageUrl(Long userId, ChangeProfileImageRequestDTO request) {
        UserEntity user = getUserOrThrow(userId);
        user.changeProfileImageUrl(request.profileImageUrl());
        return toMeResponse(user);
    }

    /**
     * 유저의 온보딩 정보가 존재하는지 판단하는 메서드.
     * UserProfile이 존재하는 경우 true, 이외에는 false를 던진다.
     * @param userId 프로필 존재여부를 검증할 유저의 id
     * @return 온보딩 정보가 존재한다면 true, 아니라면 false
     */
    @Transactional(readOnly = true)
    public Boolean onboardingStatus(Long userId) {
        return  userProfileRepository.existsById(userId);
    }

    private UserEntity getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private MeResponseDTO toMeResponse(UserEntity user) {
        return MeResponseDTO.builder()
                .email(user.getEmail())
                .handle(user.getHandle())
                .nickname(user.getNickname())
                .role(user.getRole().toString())
                .status(user.getStatus().toString())
                .profileImageUrl(user.getProfileImageUrl())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public TrainerInfoResponseDTO getTrainerInfo(Long userId) {
        UserEntity user = getUserOrThrow(userId);
        TrainerInfoEntity tInfo = trainerInfoRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        return TrainerInfoResponseDTO.builder()
                .applicationStatus(tInfo.getApplicationStatus())
                .licenseUrlsJson(parseLicenseUrls(tInfo.getLicenseUrlsJson()))
                .bio(tInfo.getBio())
                .rejectReason(tInfo.getRejectReason())
                .approvedAt(tInfo.getApprovedAt())
                .build();
    }

    private List<String> parseLicenseUrls(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            // 데이터 깨졌을 때 방어
            throw new BusinessException(ErrorCode.INVALID_DATA);
        }
    }
}
