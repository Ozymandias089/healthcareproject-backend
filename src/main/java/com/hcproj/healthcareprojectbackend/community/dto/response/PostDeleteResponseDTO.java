package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record PostDeleteResponseDTO(
        String message,
        Instant deletedAt
) {
    public static PostDeleteResponseDTO of(Instant deletedAt) {
        return new PostDeleteResponseDTO("POST_DELETED", deletedAt);
    }
}