package com.hcproj.healthcareprojectbackend.me.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.security.jwt.TokenVersionStore;
import com.hcproj.healthcareprojectbackend.me.dto.request.PasswordChangeRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.request.WithdrawalRequestDTO;
import com.hcproj.healthcareprojectbackend.me.dto.response.MeResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public MeResponseDTO getMe(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return new MeResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getHandle(),
                user.getNickname(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

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
    public void withdraw(Long userId, WithdrawalRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 이미 탈퇴된 회원의 경우 409 CONFLICT 반환
        if (user.getStatus() == UserStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }

        // 비밀번호 검증(본인 확인)
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        user.withdraw();

        tokenVersionStore.bump(userId);
    }
}
