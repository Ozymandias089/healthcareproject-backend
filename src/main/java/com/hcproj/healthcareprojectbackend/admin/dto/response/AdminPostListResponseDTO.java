package com.hcproj.healthcareprojectbackend.admin.dto.response;

import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AdminPostListResponseDTO(
        long total,
        List<AdminPostItemDTO> list
) {

    @Builder
    public record AdminPostItemDTO(
            Long postId,
            AuthorDTO author,
            String category,
            String title,
            Long viewCount,
            Boolean isNotice,
            PostStatus status,
            Instant createdAt
    ) {}

    @Builder
    public record AuthorDTO(
            String nickname,
            String handle
    ) {}
}