package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminUserListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.repository.AdminUserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
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

    private final AdminUserRepository adminUserRepository;

    // [기존 기능] 관리자 권한 승격
    @Transactional
    public void promoteToAdmin(String targetHandle) {
        // 유저가 존재하는지 확인 (Optional 처리)
        if (!adminUserRepository.existsByHandle(targetHandle)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 별도 Setter 없이 쿼리로 바로 업데이트
        adminUserRepository.updateUserRole(targetHandle, UserRole.ADMIN);
    }

    // [신규 기능] 전체 회원 목록 조회
    @Transactional(readOnly = true)
    public AdminUserListResponseDTO getUserList(int page, int size, String roleStr, String keyword) {

        // 1. Role 파라미터 변환 (String -> Enum)
        UserRole role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 Role 값이 오면 필터링을 하지 않음
            }
        }

        // 2. 페이징 설정 (가입일 최신순 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 3. DB 조회 (동적 쿼리 실행)
        Page<UserEntity> userPage = adminUserRepository.findAllWithFilters(role, keyword, pageable);

        // 4. 결과 매핑 (Entity -> DTO)
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
                // UserEntity에 status 필드가 있다면 user.getStatus().name() 등으로 변경
                .status("ACTIVE")
                .createdAt(user.getCreatedAt())
                .build();
    }
}