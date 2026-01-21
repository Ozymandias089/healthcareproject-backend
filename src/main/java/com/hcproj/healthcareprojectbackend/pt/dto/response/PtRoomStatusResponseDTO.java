package com.hcproj.healthcareprojectbackend.pt.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import java.time.Instant;

public record PtRoomStatusResponseDTO(
        Long ptRoomId,
        PtRoomStatus status,
        Instant scheduledAt // START 시 시간이 기록되므로 필요
) {}