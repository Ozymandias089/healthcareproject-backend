package com.hcproj.healthcareprojectbackend.upload.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

/**
 * 파일 업로드 유형을 정의하는 Enum.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>업로드 목적에 따라 저장 폴더를 분리</li>
 *   <li>업로드 허용 파일 확장자를 타입별로 제한</li>
 * </ul>
 *
 * <p><b>보안 정책</b></p>
 * <ul>
 *   <li>확장자 화이트리스트 기반 검증</li>
 *   <li>타입별로 허용되는 파일 종류를 명확히 분리</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum UploadType {

    /**
     * 사용자 프로필 이미지 업로드
     */
    PROFILE("profiles", Set.of("jpg", "jpeg", "png", "gif", "webp")),
    /**
     * 커뮤니티 게시글 첨부 파일 업로드
     */
    POST("posts", Set.of("jpg", "jpeg", "png", "gif", "webp")),
    /**
     * 트레이너 자격증/증빙 자료 업로드
     *
     * <p>
     * 이미지 + 문서 파일 업로드 허용
     * </p>
     */
    TRAINER("trainers", Set.of(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "hwp"
    ));

    /** 업로드 대상 폴더 이름 */
    private final String folder;
    /** 허용된 파일 확장자 목록 */
    private final Set<String> allowedExtensions;

    /**
     * 파일 확장자가 허용되는지 확인한다.
     *
     * @param extension 파일 확장자 (점 제외)
     * @return 허용된 확장자면 true
     */
    public boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * 문자열 값으로 UploadType을 조회한다.
     *
     * <p>
     * 대소문자를 구분하지 않는다.
     * </p>
     *
     * @param type 업로드 타입 문자열
     * @return 대응되는 UploadType
     * @throws IllegalArgumentException 유효하지 않은 타입인 경우
     */
    public static UploadType fromString(String type) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid upload type: " + type));
    }
}