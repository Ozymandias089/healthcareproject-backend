package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import java.util.List;

/**
 * 게시글 목록 조회 응답 DTO
 */
@Builder
public record PostListResponseDTO(
        List<PostSummaryDto> list, // PostSimpleDTO 대신 PostSummaryDto 사용
        Long nextCursorId        // 다음 조회를 위한 커서 ID
) {}