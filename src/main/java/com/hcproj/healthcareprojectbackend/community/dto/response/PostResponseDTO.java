package com.hcproj.healthcareprojectbackend.community.dto.response;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Builder
public record PostResponseDTO(
        Long postId,
        AuthorDTO author,
        String category,
        String title,
        Long viewCount,
        int commentCount,
        String content,
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        List<CommentDTO> comments // [수정] Object -> CommentDTO로 구체화
) {
    public static PostResponseDTO from(PostEntity post, UserEntity user) {
        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(AuthorDTO.from(user))
                .category(post.getCategory())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(0) // 추후 댓글 구현 시 수정 필요
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .updatedAt(post.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .deletedAt(post.getDeletedAt() == null ? null :
                        post.getDeletedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .comments(Collections.emptyList()) // 아직 댓글 기능이 없으므로 빈 리스트 반환
                .build();
    }

    // [작성자 정보 DTO]
    @Builder
    public record AuthorDTO(String nickname, String handle) {
        public static AuthorDTO from(UserEntity user) {
            return new AuthorDTO(user.getNickname(), user.getHandle());
        }
    }

    // [추가] 명세서에 맞춘 댓글 DTO (대댓글 children 포함)
    @Builder
    public record CommentDTO(
            Long commentId,
            String content,
            AuthorDTO author,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            List<CommentDTO> children // 대댓글 리스트 (재귀 구조)
    ) {}
}