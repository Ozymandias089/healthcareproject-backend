package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminPromoteRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminUserService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.me.dto.response.MessageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PatchMapping("/promote")
    public ApiResponse<MessageResponseDTO> promoteUser(
            @CurrentUserId Long adminId,
            @Valid @RequestBody AdminPromoteRequestDTO request
    ) {
        adminUserService.promoteToAdmin(request.targetHandle());
        return ApiResponse.ok(new MessageResponseDTO("USER_PROMOTED_TO_ADMIN"));
    }
}