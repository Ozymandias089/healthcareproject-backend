package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record PostDetailResponseDTO(
        Long postId,
        AuthorDTO author,          // [변경] writerNickname -> AuthorDTO 객체
        String category,
        String title,
        String content,
        Long viewCount,
        Long commentCount,         // [추가] 댓글 수
        Long likeCount,
        Instant createdAt,
        Instant updatedAt,         // [추가] 수정일 (null 가능)
        boolean isOwner,
        List<PostResponseDTO.CommentDTO> comments
) {
    @Builder
    public record AuthorDTO(
            Long userId,
            String nickname,
            String handle,
            String profileImageUrl // 프론트엔드 Author 타입에 맞춰 추가
    ) {}
}