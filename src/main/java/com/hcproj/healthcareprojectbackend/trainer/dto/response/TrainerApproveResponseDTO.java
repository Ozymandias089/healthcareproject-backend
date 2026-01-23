package com.hcproj.healthcareprojectbackend.trainer.dto.response;

import java.time.Instant;

public record TrainerApproveResponseDTO(
        String handle,
        String role,
        Instant approvedAt
) {
    public static TrainerApproveResponseDTO of(String handle, String role, Instant approvedAt) {
        return new TrainerApproveResponseDTO(handle, role, approvedAt);
    }
}