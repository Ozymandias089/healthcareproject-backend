package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record CommentCreateResponseDTO(
        String message,
        Long commentId,
        Instant createdAt
) {
    public static CommentCreateResponseDTO of(Long commentId, Instant createdAt) {
        return new CommentCreateResponseDTO("COMMENT_CREATED", commentId, createdAt);
    }
}