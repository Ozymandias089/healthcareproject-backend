package com.hcproj.healthcareprojectbackend.admin.dto.response;

import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record AdminUserListResponseDTO(
        long total,
        List<AdminUserDetailDTO> list
) {
    @Builder
    public record AdminUserDetailDTO(
            Long userId,
            String email,
            String handle,
            String nickname,
            String role,   // USER, TRAINER, ADMIN
            String status, // ACTIVE, SUSPENDED, WITHDRAWN
            Instant createdAt
    ) {}
}