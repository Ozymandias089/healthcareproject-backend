package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequestDTO(
        Long parentId, // 대댓글인 경우 부모 ID (없으면 null)
        @NotBlank String content // 댓글 내용
) {}
