package com.hcproj.healthcareprojectbackend.pt.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PtRoomParticipantsResponseDTO(
        Long ptRoomId,
        int count,
        List<UserDTO> users
) {
    @Builder
    public record UserDTO(
            String handle,
            String nickname,
            String profileImageUrl,
            String role // "TRAINER" or "USER"
    ) {}
}
