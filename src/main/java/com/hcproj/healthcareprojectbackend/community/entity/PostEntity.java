package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private Category category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice;

    // ❌ [삭제됨] private LocalDateTime deletedAt;
    // (BaseTimeEntity에 이미 들어있으므로 여기서 또 쓰면 에러납니다!)

    @Formula("(SELECT count(1) FROM comments c WHERE c.post_id = post_id AND c.deleted_at IS NULL)")
    private Long commentCount;

    public Long getCommentCount() {
        return this.commentCount == null ? 0L : this.commentCount;
    }

    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void update(String title, String content, Category category, Boolean isNotice) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isNotice = (isNotice != null) ? isNotice : false;
    }
}