package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminDashboardResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminDashboardFacade;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardFacade adminDashboardFacade;

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponseDTO> getDashboard(@CurrentUserId Long userId) {
        return ApiResponse.ok(adminDashboardFacade.getDashboardData());
    }
}