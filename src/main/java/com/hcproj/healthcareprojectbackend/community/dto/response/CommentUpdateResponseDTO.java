package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record CommentUpdateResponseDTO(
        String message,
        Long commentId,
        Instant createdAt // 명세서에 createdAt으로 되어있어 그대로 둠 (보통은 updatedAt)
) {
    public static CommentUpdateResponseDTO of(Long commentId, Instant createdAt) {
        return new CommentUpdateResponseDTO("COMMENT_UPDATED", commentId, createdAt);
    }
}