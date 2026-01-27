package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class PostListResponseDTO {

    private List<PostSimpleDTO> list; // 게시글 목록
    private Long nextCursorId;        // 다음 커서 ID

    // PostService 내부에서 사용하는 DTO (static 필수)
    @Getter
    @Builder
    public static class PostSimpleDTO {
        private Long postId;
        private String category;
        private String title;
        private Long viewCount;     // int -> Long
        private Long commentCount;  // int -> Long
        private Long likeCount;     // int -> Long
        private Instant createdAt;
        private Boolean isNotice;
    }
}