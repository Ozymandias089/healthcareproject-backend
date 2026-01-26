package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.UserStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminUserListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.UserStatusUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus; // [필수] 이거 꼭 있어야 해요!
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    // 1. 관리자 권한 승격 (기존 코드)
    @Transactional
    public void promoteToAdmin(String targetHandle) {
        UserEntity target = userRepository.findByHandle(targetHandle)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        target.makeAdmin();
    }

    // 2. 전체 회원 목록 조회 (기존 코드)
    @Transactional(readOnly = true)
    public AdminUserListResponseDTO getUserList(int page, int size, String roleStr, String keyword) {
        // Role 파라미터 변환
        UserRole role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 Role 값은 무시
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserEntity> userPage = userRepository.findAllWithFilters(role, keyword, pageable);

        List<AdminUserListResponseDTO.AdminUserDetailDTO> dtoList = userPage.stream()
                .map(this::convertToDetailDTO)
                .toList();

        return AdminUserListResponseDTO.builder()
                .total(userPage.getTotalElements())
                .list(dtoList)
                .build();
    }

    // 3. 회원 상태 변경 (차단/해제)
    @Transactional
    public UserStatusUpdateResponseDTO updateUserStatus(Long userId, UserStatusUpdateRequestDTO request) {
        // (1) 회원 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // (2) 변경할 상태 파싱
        UserStatus newStatus;
        try {
            newStatus = UserStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // (3) 상태 변경
        String previousStatus = user.getStatus().name();
        user.updateStatus(newStatus);

        // (4) 결과 메시지 생성 (여기 수정됨!)
        String message = "회원 상태가 변경되었습니다.";

        // [수정] BANNED -> SUSPENDED 로 변경
        if (newStatus == UserStatus.SUSPENDED) {
            message = "회원이 이용 정지되었습니다.";
        } else if (newStatus == UserStatus.ACTIVE) {
            message = "이용 정지가 해제되었습니다.";
        }

        // (5) 응답 반환
        return UserStatusUpdateResponseDTO.builder()
                .userId(user.getId())
                .previousStatus(previousStatus)
                .currentStatus(newStatus.name())
                .message(message)
                .build();
    }

    // Entity -> DTO 변환 헬퍼 메서드
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
}