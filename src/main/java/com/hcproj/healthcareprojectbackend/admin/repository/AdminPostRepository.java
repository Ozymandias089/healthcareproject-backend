package com.hcproj.healthcareprojectbackend.admin.repository;

import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

/**
 * 관리자 게시판 리포지토리
 */
public interface AdminPostRepository extends JpaRepository<PostEntity, Long> {

    /**
     * 관리자용 게시글 목록 조회 (페이지네이션 + 필터링)
     * - category: 카테고리 필터 (null이면 전체)
     * - status: 게시글 상태 필터 (null이면 전체)
     * - keyword: 제목 또는 작성자 닉네임 검색 (null이면 전체)
     */

    // 상태별 게시글 수 (POSTED, DELETED)
    long countByStatus(PostStatus status);

    // 오늘 작성된 게시글 수
    long countByCreatedAtAfter(Instant startOfDay);

    @Query("SELECT p FROM PostEntity p " +
            "LEFT JOIN UserEntity u ON p.userId = u.id " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
            "ORDER BY p.postId DESC")
    Page<PostEntity> findAdminPostList(
            @Param("category") String category,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 관리자용 게시글 전체 개수 조회 (필터링 적용)
     */
    @Query("SELECT COUNT(p) FROM PostEntity p " +
            "LEFT JOIN UserEntity u ON p.userId = u.id " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:keyword IS NULL OR p.title LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    long countAdminPostList(
            @Param("category") String category,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword
    );
}