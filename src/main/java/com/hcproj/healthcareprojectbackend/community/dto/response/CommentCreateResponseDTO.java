package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.LocalDateTime;

public record CommentCreateResponseDTO(
        String message,
        Long commentId,
        LocalDateTime createdAt
) {
    public static CommentCreateResponseDTO of(Long commentId, LocalDateTime createdAt) {
        return new CommentCreateResponseDTO("COMMENT_CREATED", commentId, createdAt);
    }
}