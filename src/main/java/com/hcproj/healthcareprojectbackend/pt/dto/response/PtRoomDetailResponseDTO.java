package com.hcproj.healthcareprojectbackend.pt.dto.response;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;
import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomType;
import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record PtRoomDetailResponseDTO(
        Long ptRoomId,
        String title,
        String description,
        Instant scheduledAt,
        TrainerDTO trainer,
        String entryCode,
        Boolean isPrivate,
        PtRoomType roomType,
        PtRoomStatus status,
        String janusRoomKey,
        Integer maxParticipants,
        ParticipantsDTO participants
) {
    public record TrainerDTO(String nickname, String handle, String profileImageUrl) {}

    public record ParticipantsDTO(int count, List<UserDTO> users) {}

    public record UserDTO(String nickname, String handle) {}
}