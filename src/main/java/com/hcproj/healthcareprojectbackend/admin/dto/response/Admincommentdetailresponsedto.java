package com.hcproj.healthcareprojectbackend.admin.dto.response;

import com.hcproj.healthcareprojectbackend.community.entity.CommentStatus;

import java.time.Instant;

/**
 * 관리자용 댓글 상세 조회 응답 DTO
 */
public record Admincommentdetailresponsedto(
        Long commentId,
        Long postId,
        String postTitle,
        AuthorDTO author,
        String content,
        CommentStatus status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public record AuthorDTO(
            Long userId,
            String nickname,
            String handle
    ) {}
}