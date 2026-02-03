package com.hcproj.healthcareprojectbackend.admin.dto.response;

import lombok.Builder;

@Builder
public record UserStatusUpdateResponseDTO(
        Long userId,
        String previousStatus,
        String currentStatus,
        String message
) {}