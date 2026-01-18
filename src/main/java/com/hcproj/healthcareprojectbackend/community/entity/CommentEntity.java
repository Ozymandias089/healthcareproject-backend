package com.hcproj.healthcareprojectbackend.community.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "status", nullable = false)
    private CommentStatus status; // POSTED | DELETED
}
