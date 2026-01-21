package com.hcproj.healthcareprojectbackend.pt.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtReservationStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PtReservationResponseDTO(
        Long ptReservationId,
        Long ptRoomId,
        PtReservationStatus status,
        Instant reservedAt,
        Instant cancelledAt
) {}