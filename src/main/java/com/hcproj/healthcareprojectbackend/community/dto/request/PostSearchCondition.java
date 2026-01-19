package com.hcproj.healthcareprojectbackend.community.dto.request;

public record PostSearchCondition(
        String category,    // ALL, FREE, QUESTION, INFO
        String searchBy,    // TITLE, CONTENT, TITLE_CONTENT, AUTHOR
        String q,           // 검색어
        Long cursorId,      // 마지막으로 본 게시글 ID
        Integer size        // 가져올 개수 (기본값 처리는 Controller에서)
) {}