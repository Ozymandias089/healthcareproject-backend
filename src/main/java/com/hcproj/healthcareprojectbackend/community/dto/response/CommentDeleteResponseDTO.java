package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.LocalDateTime;

public record CommentDeleteResponseDTO(
        String message,
        LocalDateTime deletedAt
) {
    public static CommentDeleteResponseDTO of(LocalDateTime deletedAt) {
        return new CommentDeleteResponseDTO("COMMENT_DELETED", deletedAt);
    }
}