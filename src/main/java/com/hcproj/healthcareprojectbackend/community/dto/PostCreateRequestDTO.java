// 파일명: PostCreateRequestDTO.java
package com.hcproj.healthcareprojectbackend.community.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequestDTO(
        @NotBlank String category,  // "QUESTION", "FREE" 등
        @NotBlank String title,

        @NotBlank
        String content,             // Toast UI Editor가 보낸 Markdown 텍스트 (예: "**굵게** \n - 리스트")

        Boolean isNotice            // 공지 여부 (null 가능하므로 Boolean wrapper 사용)
) {}