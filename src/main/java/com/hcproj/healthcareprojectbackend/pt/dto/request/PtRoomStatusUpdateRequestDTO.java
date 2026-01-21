package com.hcproj.healthcareprojectbackend.pt.dto.request;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import jakarta.validation.constraints.NotNull;

public record PtRoomStatusUpdateRequestDTO(
        @NotNull
        PtRoomStatus status // ★ 여기가 action -> status로 바뀜!
) {
}