package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 댓글({@link CommentEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p>
 * 현재는 게시글 기준 댓글 목록 조회를 제공하며,
 * 정렬은 기본적으로 댓글 ID 기준(작성 순)을 전제로 한다.
 * </p>
 */
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    /**
     * 특정 게시글의 댓글 목록을 조회한다.
     *
     * <p>
     * Spring Data JPA 기본 정렬은 보장되지 않으므로,
     * 작성 순 정렬이 필요하다면 {@code OrderByCommentIdAsc/Desc} 또는 Sort/Pageable 사용을 고려한다.
     * </p>
     *
     * @param postId 게시글 ID
     */
    List<CommentEntity> findAllByPostId(Long postId);

    // [추가] 게시글 상세 조회 시 댓글 개수 카운트용 (PostService에서 사용)
    long countByPostId(Long postId);
}