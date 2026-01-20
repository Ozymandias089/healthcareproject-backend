package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.time.Instant;

public record PostSummaryDto(
        Long postId,
        String category,
        Boolean isNotice,
        String title,
        String nickname,    // 작성자 닉네임 (UserEntity에서 가져옴)
        String handle,      // 작성자 핸들
        Instant createdAt,
        Long commentCount,  // 댓글 수 (@Formula로 가져옴)
        Long viewCount
) {}