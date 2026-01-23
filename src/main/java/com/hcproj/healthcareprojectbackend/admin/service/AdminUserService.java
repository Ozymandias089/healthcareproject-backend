package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
