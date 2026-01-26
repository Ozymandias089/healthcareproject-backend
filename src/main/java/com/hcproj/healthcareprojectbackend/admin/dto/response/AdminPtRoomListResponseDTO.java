package com.hcproj.healthcareprojectbackend.admin.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AdminPtRoomListResponseDTO(
        long total,
        List<AdminPtRoomItemDTO> list
) {
    @Builder
    public record AdminPtRoomItemDTO(
            Long ptRoomId,
            TrainerDTO trainer,
            String title,
            String description,
            PtRoomType roomType,
            Instant scheduledStartAt,
            Integer maxParticipants,
            Boolean isPrivate,
            String entryCode,
            PtRoomStatus status,
            Instant createdAt
    ) {}

    @Builder
    public record TrainerDTO(
            String nickname,
            String handle
    ) {}
}