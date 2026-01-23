package com.hcproj.healthcareprojectbackend.admin.dto.response;

import java.time.Instant;

public record TrainerRejectResponseDTO(
        String handle,
        String status,
        String reason,
        Instant rejectedAt
) {
    public static TrainerRejectResponseDTO of(String handle, String status, String reason, Instant rejectedAt) {
        return new TrainerRejectResponseDTO(handle, status, reason, rejectedAt);
    }
}