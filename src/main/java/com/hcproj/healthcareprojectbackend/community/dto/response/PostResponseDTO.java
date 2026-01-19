package com.hcproj.healthcareprojectbackend.community.dto.response;

import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId; // 이거 필요!
import java.util.Collections;
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        List<CommentDTO> comments
) {
    public static PostResponseDTO from(PostEntity post) {
        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(AuthorDTO.from(post.getUser()))
                .category(post.getCategory().name())
                .isNotice(post.getIsNotice())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount().intValue())
                .content(post.getContent())
                .status(post.getStatus())
                // ▼ [수정] Instant -> LocalDateTime 변환
                .createdAt(post.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .updatedAt(post.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .deletedAt(post.getDeletedAt() == null ? null : post.getDeletedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .comments(Collections.emptyList())
                .build();
    }

    // ... AuthorDTO, CommentDTO는 그대로 두세요 ...
    @Builder
    public record AuthorDTO(String nickname, String handle) {
        public static AuthorDTO from(com.hcproj.healthcareprojectbackend.auth.entity.UserEntity user) {
            if (user == null) return new AuthorDTO("(알수없음)", "");
            return new AuthorDTO(user.getNickname(), user.getHandle());
        }
    }
    @Builder public record CommentDTO(Long commentId, String content, AuthorDTO author, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt, List<CommentDTO> children) {}
}