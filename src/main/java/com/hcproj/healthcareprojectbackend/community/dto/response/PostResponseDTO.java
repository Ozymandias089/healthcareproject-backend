package com.hcproj.healthcareprojectbackend.community.dto.response;

import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import lombok.Builder;
import java.time.Instant; //
import java.util.List;

@Builder
public record PostResponseDTO(
        Long postId,
        AuthorDTO author,
        String category,
        Boolean isNotice,
        String title,
        Long viewCount,
        int commentCount,
        String content,
        PostStatus status,
        Instant createdAt, //
        Instant updatedAt,
        Instant deletedAt,
        List<CommentDTO> comments
) {
    public record AuthorDTO(String nickname, String handle) {}

    @Builder
    public record CommentDTO(
            Long commentId,
            String content,
            AuthorDTO author,
            Instant createdAt, //
            Instant updatedAt,
            Instant deletedAt,
            List<CommentDTO> children
    ) {}
}