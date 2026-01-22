package com.hcproj.healthcareprojectbackend.pt.dto.response;

import lombok.Builder;
import java.time.Instant;

@Builder
public record PtRoomKickResponseDTO(
        Long ptRoomId,
        KickedUserDTO kickedUser,
        Instant kickedAt,
        ParticipantCountDTO participants
) {
    public record KickedUserDTO(
            String handle,
            String nickname,
            String profileImageUrl
    ) {}

    public record ParticipantCountDTO(
            long count
    ) {}
}