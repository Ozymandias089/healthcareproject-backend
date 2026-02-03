package com.hcproj.healthcareprojectbackend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

// 관리자 화상 PT 방 강제 종료 요청
public record AdminPtRoomForceCloseRequestDTO(
        @NotBlank(message = "종료 사유는 필수입니다.")
        String reason
) {}