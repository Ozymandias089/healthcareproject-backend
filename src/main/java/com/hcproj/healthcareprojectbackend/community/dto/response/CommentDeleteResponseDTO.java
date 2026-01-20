package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record CommentDeleteResponseDTO(
        String message,
        Instant deletedAt
) {
    public static CommentDeleteResponseDTO of(Instant deletedAt) {
        return new CommentDeleteResponseDTO("COMMENT_DELETED", deletedAt);
    }
}