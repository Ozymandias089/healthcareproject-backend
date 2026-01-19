// 파일명: PostResponseDTO.java
package com.hcproj.healthcareprojectbackend.community.dto.response;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
public record PostResponseDTO(
        Long postId,
        AuthorDTO author,       // 작성자 정보 { 닉네임, 핸들 }
        String category,
        String title,
        Long viewCount,
        int commentCount,
        String content,         // Markdown 원문 반환
        PostStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt,
        List<Object> comments
) {
    public static PostResponseDTO from(PostEntity post, UserEntity user) {
        return PostResponseDTO.builder()
                .postId(post.getPostId())
                .author(AuthorDTO.from(user)) // 아래 내부 레코드 사용
                .category(post.getCategory())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .commentCount(0)
                .content(post.getContent())
                .status(post.getStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(null)
                .comments(Collections.emptyList())
                .build();
    }

    // 작성자 정보 DTO
    @Builder
    public record AuthorDTO(String nickname, String handle) {
        public static AuthorDTO from(UserEntity user) {
            return new AuthorDTO(user.getNickname(), user.getHandle());
        }
    }
}