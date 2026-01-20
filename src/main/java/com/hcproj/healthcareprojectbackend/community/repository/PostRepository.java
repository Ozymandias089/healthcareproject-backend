package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 공지사항 조회 (ID 기반)
    List<PostEntity> findByIsNoticeTrueAndStatusOrderByPostIdDesc(com.hcproj.healthcareprojectbackend.community.entity.PostStatus status);

    // 목록 조회 (커서 기반 페이지네이션)
    // User와 조인하지 않고 Post만 가져옵니다.
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (:q IS NULL OR (p.title LIKE %:q% OR p.content LIKE %:q%)) " +
            "AND p.status = 'POSTED' " + // 삭제 안 된 것만
            "ORDER BY p.postId DESC")
    List<PostEntity> findPostList(
            @Param("cursorId") Long cursorId,
            @Param("category") String category, // String으로 받음
            @Param("q") String q,
            Pageable pageable
    );
}