package com.hcproj.healthcareprojectbackend.pt.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record PtRoomListResponseDTO(
        List<ItemDTO> items,
        PageInfo pageInfo
) {
    @Builder
    public record ItemDTO(
            Long ptRoomId,
            String title,
            PtRoomType roomType,
            PtRoomStatus status,
            Instant scheduledAt,
            Boolean isPrivate,
            TrainerDTO trainer,
            ParticipantsDTO participants
    ) {}

    public record TrainerDTO(String nickname, String handle, String profileImageUrl) {}

    public record ParticipantsDTO(int current, int max) {}

    public record PageInfo(Long nextCursorId, boolean hasNext, int size) {}
}