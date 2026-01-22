package com.hcproj.healthcareprojectbackend.pt.dto.response;

import java.time.Instant;
import java.util.List;

public record PtReservationListResponseDTO(
        List<ReservedUserDTO> reservedUser
) {
    public record ReservedUserDTO(
            Long ptReservationId,
            UserProfileDTO user,
            Instant createdAt
    ) {}

    public record UserProfileDTO(
            String handle,
            String nickname,
            String profileImageUrl
    ) {}
}