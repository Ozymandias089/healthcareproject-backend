package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.LocalDateTime;

public record CommentUpdateResponseDTO(
        String message,
        Long commentId,
        LocalDateTime createdAt // 명세서에 createdAt으로 되어있어 그대로 둠 (보통은 updatedAt)
) {
    public static CommentUpdateResponseDTO of(Long commentId, LocalDateTime createdAt) {
        return new CommentUpdateResponseDTO("COMMENT_UPDATED", commentId, createdAt);
    }
}