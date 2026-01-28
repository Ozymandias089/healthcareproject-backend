package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 커뮤니티 게시글을 나타내는 엔티티.
 *
 * <p><b>주요 필드</b></p>
 * <ul>
 *   <li>{@code category}: 게시글 분류(문자열 기반)</li>
 *   <li>{@code isNotice}: 공지 여부</li>
 *   <li>{@code viewCount}: 조회수(증가 메서드 제공)</li>
 * </ul>
 *
 * <p><b>조회수 정책</b></p>
 * <ul>
 *   <li>{@link #increaseViewCount()}는 null 안전하게 증가시킨다.</li>
 * </ul>
 *
 * <p><b>수정 정책</b></p>
 * <ul>
 *   <li>{@link #update(String, String, String, Boolean)}는 부분 업데이트 방식</li>
 *   <li>null/blank인 값은 기존 값을 유지한다.</li>
 * </ul>
 *
 * <p><b>삭제 정책</b></p>
 * <ul>
 *   <li>물리 삭제 대신 상태({@code DELETED}) 변경 + 소프트 삭제 적용</li>
 *   <li>{@link #delete()}는 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "posts")
public class PostEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category", nullable = false, length = 30)
    private String category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatus status;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice;

    /**
     * 조회수를 1 증가시킨다.
     *
     * <p>
     * viewCount가 null인 데이터가 존재할 수 있다는 가정 하에
     * null이면 0으로 간주하고 증가시킨다.
     * </p>
     */
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    /**
     * 게시글 내용을 부분 업데이트한다.
     *
     * <p>
     * null 또는 blank인 문자열 파라미터는 무시되어 기존 값이 유지된다.
     * isNotice는 null이면 무시된다.
     * </p>
     *
     * @param title    변경할 제목(선택)
     * @param content  변경할 본문(선택)
     * @param category 변경할 카테고리(선택)
     * @param isNotice 공지 여부(선택)
     */
    public void update(String title, String content, String category, Boolean isNotice) {

        if (title != null && !title.isBlank()) {
            this.title = title;
        }

        if (content != null && !content.isBlank()) {
            this.content = content;
        }

        if (category != null && !category.isBlank()) {
            this.category = category;
        }

        if (isNotice != null) {
            this.isNotice = isNotice;
        }

    }

    /**
     * 게시글을 삭제 처리한다(소프트 삭제).
     *
     * <p>
     * 상태를 {@code DELETED}로 변경하고 삭제 시각을 기록한다.
     * 이미 삭제된 경우에는 아무 동작도 하지 않는다(멱등성 보장).
     * </p>
     */
    public void delete() {
        // 1. 멱등성 체크
        if (this.status == PostStatus.DELETED || this.isDeleted()) {
            return;
        }

        // 2. 상태 변경 및 삭제 시간 기록
        this.status = PostStatus.DELETED;
        this.markDeleted(); // BaseTimeEntity의 deletedAt에 현재 시간 기록
    }

    // 게시글 복구
    public void restore() {
        if (this.status == PostStatus.POSTED) return;
        this.status = PostStatus.POSTED;
        this.undoDeletion();
    }
}