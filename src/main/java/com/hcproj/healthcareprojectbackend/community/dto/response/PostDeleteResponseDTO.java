package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.LocalDateTime;

public record PostDeleteResponseDTO(
        String message,
        LocalDateTime deletedAt
) {
    public static PostDeleteResponseDTO of(LocalDateTime deletedAt) {
        return new PostDeleteResponseDTO("POST_DELETED", deletedAt);
    }
}