package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

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

    public void delete() {
        // 1. 멱등성 체크
        if (this.status == PostStatus.DELETED || this.isDeleted()) {
            return;
        }

        // 2. 상태 변경 및 삭제 시간 기록
        this.status = PostStatus.DELETED;
        this.markDeleted(); // BaseTimeEntity의 deletedAt에 현재 시간 기록
    }
}