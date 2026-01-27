package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public class PostListResponseDTO {

    private List<PostSimpleDTO> list; // 게시글 목록
    private Long nextCursorId;        // 다음 커서 ID

    // PostService 내부에서 사용하는 DTO (static 필수)
    @Builder
    public record PostSimpleDTO(
            Long postId,
            String category,
            String title,
            Long viewCount,
            Long commentCount,
            Long likeCount,
            Instant createdAt,
            Boolean isNotice
    ) {}
}