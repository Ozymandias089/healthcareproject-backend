package com.hcproj.healthcareprojectbackend.upload.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

/**
 * 업로드 타입 Enum
 */
@Getter
@RequiredArgsConstructor
public enum UploadType {

    PROFILE("profiles", Set.of("jpg", "jpeg", "png", "gif", "webp")),
    POST("posts", Set.of("jpg", "jpeg", "png", "gif", "webp")),
    TRAINER("trainers", Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "hwp"
    ));

    private final String folder;
    private final Set<String> allowedExtensions;

    /**
     * 확장자 허용 여부 확인
     */
    public boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * 문자열로 UploadType 찾기
     */
    public static UploadType fromString(String type) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid upload type: " + type));
    }
}