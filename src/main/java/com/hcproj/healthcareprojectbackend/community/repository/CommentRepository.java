package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import com.hcproj.healthcareprojectbackend.community.entity.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // ▼ [수정됨] PostId -> Post_PostId (언더스코어 추가)
    // 이유: PostEntity의 PK가 'id'가 아니라 'postId'이기 때문에,
    // 스프링에게 "Post 객체 안에 있는 PostId를 찾아라"고 명확히 지시해야 합니다.
    List<CommentEntity> findAllByPost_PostIdAndStatusOrderByCommentIdAsc(Long postId, CommentStatus status);

    // [추가된 상세 조회용 메서드] (이건 그대로 유지)
    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.post.postId = :postId ORDER BY c.commentId ASC")
    List<CommentEntity> findAllByPostIdForDetail(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE CommentEntity c SET c.status = 'DELETED', c.deletedAt = :now WHERE c.commentId = :commentId")
    void softDelete(@Param("commentId") Long commentId, @Param("now") LocalDateTime now);
}