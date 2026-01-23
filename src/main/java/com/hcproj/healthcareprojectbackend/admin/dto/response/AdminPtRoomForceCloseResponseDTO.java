package com.hcproj.healthcareprojectbackend.admin.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record AdminPtRoomForceCloseResponseDTO(
        Long ptRoomId,
        PtRoomStatus status,
        String message,
        Instant closedAt
) {}