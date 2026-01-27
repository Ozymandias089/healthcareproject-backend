package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PostDetailResponseDTO {
    private Long postId;
    private Long userId;
    private String writerNickname; // 작성자 닉네임
    private String category;
    private String title;
    private String content;
    private Long viewCount;     // int -> Long
    private Long likeCount;     // int -> Long
    private Instant createdAt;
    private boolean isOwner;       // 본인 글 여부 확인용
}