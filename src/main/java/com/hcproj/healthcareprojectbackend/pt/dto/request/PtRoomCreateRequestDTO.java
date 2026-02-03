package com.hcproj.healthcareprojectbackend.pt.dto.request;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record PtRoomCreateRequestDTO(
        @NotNull PtRoomType roomType,
        @NotBlank String title,
        String description,
        Instant scheduledAt,
        Integer maxParticipants,
        @NotNull Boolean isPrivate
) {}