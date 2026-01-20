package com.hcproj.healthcareprojectbackend.me.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.me.dto.internal.InjuryDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.UserInjuriesResponseDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.UserProfileResponseDTO;
import com.hcproj.healthcareprojectbackend.profile.entity.UserInjuryEntity;
import com.hcproj.healthcareprojectbackend.profile.entity.UserProfileEntity;
import com.hcproj.healthcareprojectbackend.profile.repository.UserInjuryRepository;
import com.hcproj.healthcareprojectbackend.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInjuryRepository userInjuryRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        UserProfileEntity userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return UserProfileResponseDTO.builder()
                .heightCm(userProfile.getHeightCm())
                .weightKg(userProfile.getWeightKg())
                .age(userProfile.getAge())
                .gender(userProfile.getGender())
                .experienceLevel(userProfile.getExperienceLevel())
                .goalType(userProfile.getGoalType())
                .weeklyDays(userProfile.getWeeklyDays())
                .sessionMinutes(userProfile.getSessionMinutes())
                .createdAt(userProfile.getCreatedAt())
                .updatedAt(userProfile.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public UserInjuriesResponseDTO getInjuries(Long userId) {
        if (!userRepository.existsById(userId)) throw new BusinessException(ErrorCode.NOT_FOUND);

        List<UserInjuryEntity> injuries = userInjuryRepository.findAllByUserId(userId);

        return new UserInjuriesResponseDTO(
                injuries.stream().map(injury ->
                    InjuryDTO.builder()
                            .injuryId(injury.getInjuryId())
                            .injuryPart(injury.getInjuryPart())
                            .injuryLevel(injury.getInjuryLevel().toString())
                            .createdAt(injury.getCreatedAt())
                            .updatedAt(injury.getUpdatedAt())
                            .build()
                ).toList()
        );
    }
}
