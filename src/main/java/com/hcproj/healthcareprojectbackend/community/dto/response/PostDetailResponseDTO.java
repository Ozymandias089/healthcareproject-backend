package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import java.time.Instant;

@Builder
public record PostDetailResponseDTO(
        Long postId,
        Long userId,
        String writerNickname, // 작성자 닉네임
        String category,
        String title,
        String content,
        Long viewCount,       //
        Long likeCount,       //
        Instant createdAt,
        boolean isOwner       // 본인 글 여부 확인용
) {}