package com.hcproj.healthcareprojectbackend.pt.dto.request;

import jakarta.validation.constraints.NotNull;

public record PtRoomStatusUpdateRequestDTO(
        @NotNull PtRoomStatusAction action
) {}