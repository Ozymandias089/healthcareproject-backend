package com.hcproj.healthcareprojectbackend.community.dto.response;

import lombok.Builder;
import java.util.List;

@Builder
public record PostListResponseDTO(
        List<PostSummaryDto> notices, // [추가] 공지사항 리스트 (프론트 요청)
        List<PostSummaryDto> items,   // [변경] list -> items (프론트 요청)
        PageInfo pageInfo             // [추가] 페이지 정보 객체
) {
    @Builder
    public record PageInfo(
            Long nextCursorId,
            boolean hasNext,          // [추가] 다음 페이지 존재 여부
            int size                  // [추가] 요청 사이즈
    ) {}
}