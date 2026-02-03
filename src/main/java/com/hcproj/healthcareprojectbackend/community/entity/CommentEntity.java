package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 게시글 댓글을 나타내는 엔티티.
 *
 * <p><b>모델링 특징</b></p>
 * <ul>
 *   <li>{@code postId}에 종속되는 댓글</li>
 *   <li>{@code parentCommentId}가 있으면 대댓글(스레드) 구조를 구성</li>
 * </ul>
 *
 * <p><b>삭제 정책</b></p>
 * <ul>
 *   <li>물리 삭제 대신 상태({@code DELETED}) 변경 + 소프트 삭제({@link BaseTimeEntity#markDeleted()}) 적용</li>
 *   <li>{@link #delete()}는 멱등(idempotent)하게 동작한다.</li>
 * </ul>
 *
 * <p>
 * 본문(content)은 {@link Lob}으로 저장되어 길이 제한을 완화한다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "comments")
public class CommentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "content", columnDefinition = "text", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status;

    /**
     * 댓글 내용을 수정한다.
     *
     * <p>
     * 유효성(빈 문자열 허용 여부 등) 정책은 상위 서비스/유스케이스에서 통제한다.
     * </p>
     *
     * @param content 변경할 댓글 내용
     */
    public void update(String content) {
        this.content = content;
    }

    /**
     * 댓글을 삭제 처리한다(소프트 삭제).
     *
     * <p>
     * 이미 삭제된 경우 아무 동작도 하지 않는다(멱등성 보장).
     * </p>
     */
    public void delete() {
        if (this.isDeleted()) return;
        this.status = CommentStatus.DELETED;
        this.markDeleted(); // BaseTimeEntity의 Instant.now() 기록 메서드 사용
    }
}