package com.hcproj.healthcareprojectbackend.community.dto.request;

public record CommentCreateRequestDTO(
        Long parentId, // 대댓글인 경우 부모 ID (없으면 null)
        String content // 댓글 내용
) {}