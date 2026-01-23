package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminUserListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
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

    // 관리자 권한 승격
    @Transactional
    public void promoteToAdmin(String targetHandle) {
        UserEntity target = userRepository.findByHandle(targetHandle)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        target.makeAdmin();
    }

    // 전체 회원 목록 조회
    @Transactional(readOnly = true)
    public AdminUserListResponseDTO getUserList(int page, int size, String roleStr, String keyword) {
        // 1. Role 파라미터 변환 (String -> Enum)
        UserRole role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 Role 값은 무시 (전체 조회)
            }
        }

        // 2. 페이징 설정 (가입일 최신순)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 3. DB 조회 (UserRepository에 있는 findAllWithFilters 활용)
        Page<UserEntity> userPage = userRepository.findAllWithFilters(role, keyword, pageable);

        // 4. DTO 변환
        List<AdminUserListResponseDTO.AdminUserDetailDTO> dtoList = userPage.stream()
                .map(this::convertToDetailDTO)
                .toList();

        return AdminUserListResponseDTO.builder()
                .total(userPage.getTotalElements())
                .list(dtoList)
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
