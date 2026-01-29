package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.UserStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminUserListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.UserStatusUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import com.hcproj.healthcareprojectbackend.global.util.UtilityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    // 1. 관리자 권한 승격
    @Transactional
    public void promoteToAdmin(String targetHandle) {
        UserEntity target = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        target.makeAdmin();
    }

    // 2. 전체 회원 목록 조회
    @Transactional(readOnly = true)
    public AdminUserListResponseDTO getUserList(int page, int size, String roleStr, String keyword) {
        // Role 파라미터 변환
        String roleParam = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                UserRole.valueOf(roleStr.toUpperCase());
                roleParam = roleStr.toUpperCase();
            } catch (IllegalArgumentException e) {
                // 잘못된 Role 값은 무시
            }
        }

        // 검색어 정규화
        String normalizedKeyword = UtilityProvider.normalizeKeyword(keyword);

        // 페이지네이션 계산
        int offsetSize = page * size;

        // 조회 (검색어 유무에 따라 분기)
        List<UserEntity> users;
        long total;

        if (normalizedKeyword == null) {
            // 검색어 없음
            users = userRepository.findAllWithFiltersNoKeyword(roleParam, size, offsetSize);
            total = userRepository.countAllWithFiltersNoKeyword(roleParam);
        } else {
            // 검색어 있음 - 띄어쓰기 제거 + 소문자 변환 + 와일드카드 추가
            String likePattern = "%" + normalizedKeyword.toLowerCase().replace(" ", "") + "%";
            users = userRepository.findAllWithFiltersAndKeyword(roleParam, likePattern, size, offsetSize);
            total = userRepository.countAllWithFiltersAndKeyword(roleParam, likePattern);
        }

        List<AdminUserListResponseDTO.AdminUserDetailDTO> dtoList = users.stream()
                .map(this::convertToDetailDTO)
                .toList();

        return AdminUserListResponseDTO.builder()
                .total(total)
                .list(dtoList)
                .build();
    }

    // 3. 회원 상태 변경 (차단/해제)
    @Transactional
    public UserStatusUpdateResponseDTO updateUserStatus(Long userId, UserStatusUpdateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.CANNOT_BAN_ADMIN);
        }

        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (newStatus == UserStatus.SUSPENDED) {
            if (user.getStatus() == UserStatus.SUSPENDED) {
                throw new BusinessException(ErrorCode.USER_ALREADY_BANNED);
            }
        } else if (newStatus == UserStatus.ACTIVE) {
            if (user.getStatus() != UserStatus.SUSPENDED) {
                throw new BusinessException(ErrorCode.USER_NOT_BANNED);
            }
        }

        String previousStatus = user.getStatus().name();
        user.updateStatus(newStatus);

        String message = "회원 상태가 변경되었습니다.";
        if (newStatus == UserStatus.SUSPENDED) {
            message = "회원이 이용 정지되었습니다.";
        } else if (newStatus == UserStatus.ACTIVE) {
            message = "이용 정지가 해제되었습니다.";
        }

        return UserStatusUpdateResponseDTO.builder()
                .userId(user.getId())
                .previousStatus(previousStatus)
                .currentStatus(newStatus.name())
                .message(message)
                .build();
    }

    private AdminUserListResponseDTO.AdminUserDetailDTO convertToDetailDTO(UserEntity user) {
        return AdminUserListResponseDTO.AdminUserDetailDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .handle(user.getHandle())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // ============================================================
    // Private Helper Methods
    // ============================================================

    private String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}