package com.hcproj.healthcareprojectbackend.community.dto.response;

import java.util.List;

public record PostListResponse(
        List<PostSummaryDto> notices,
        List<PostSummaryDto> items,
        PageInfo pageInfo
) {
    public record PageInfo(
            Long nextCursorId,
            boolean hasNext,
            int size
    ) {}
}