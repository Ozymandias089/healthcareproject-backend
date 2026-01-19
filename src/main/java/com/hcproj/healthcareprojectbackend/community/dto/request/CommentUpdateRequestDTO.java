package com.hcproj.healthcareprojectbackend.community.dto.request;

public record CommentUpdateRequestDTO(
        Long commentId, // 명세서 Body에 있어서 넣음 (실제로는 PathVariable ID를 주로 씀)
        String content
) {}