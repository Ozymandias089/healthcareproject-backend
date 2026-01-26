package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminPromoteRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminUserListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminUserService;
import com.hcproj.healthcareprojectbackend.admin.dto.request.UserStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.UserStatusUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 전체 회원 목록 조회 (검색 + 페이징 + 필터)
    @AdminOnly
    @GetMapping
    public ApiResponse<AdminUserListResponseDTO> getUserList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "role", required = false) String role,   // USER, TRAINER, ADMIN
            @RequestParam(name = "keyword", required = false) String keyword // 닉네임 or 이메일 검색
    ) {
        return ApiResponse.ok(adminUserService.getUserList(page, size, role, keyword));
    }

    @AdminOnly
    @PatchMapping("/promote")
    public ApiResponse<MessageResponseDTO> promoteUser(@Valid @RequestBody AdminPromoteRequestDTO request) {
        adminUserService.promoteToAdmin(request.targetHandle());
        return ApiResponse.ok(new MessageResponseDTO("USER_PROMOTED_TO_ADMIN"));
    }

    // 3. 회원 상태 변경 (차단/해제)
    @AdminOnly
    @PatchMapping("/{userId}/status")
    public ApiResponse<UserStatusUpdateResponseDTO> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequestDTO request
    ) {
        UserStatusUpdateResponseDTO response = adminUserService.updateUserStatus(userId, request);
        return ApiResponse.ok(response);
    }
}