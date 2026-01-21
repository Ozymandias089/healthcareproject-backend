package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.repository.AdminUserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    @Transactional
    public void promoteToAdmin(String targetHandle) {
        // 유저가 존재하는지 확인 (Optional 처리)
        if (!adminUserRepository.existsByHandle(targetHandle)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 별도 Setter 없이 쿼리로 바로 업데이트
        adminUserRepository.updateUserRole(targetHandle, UserRole.ADMIN);
    }
}