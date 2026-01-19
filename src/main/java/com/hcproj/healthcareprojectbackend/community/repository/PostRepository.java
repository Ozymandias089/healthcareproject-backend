package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.Category;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // 1. 공지글 조회
    List<PostEntity> findByIsNoticeTrueAndDeletedAtIsNullOrderByPostIdDesc();

    // 2. 일반글 리스트 조회 (@Query 필수!)
    @Query("SELECT p FROM PostEntity p JOIN FETCH p.user u " +
            "WHERE p.deletedAt IS NULL " +
            "AND p.isNotice = false " +
            "AND (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND (" +
            "   (:q IS NULL OR :q = '') OR " +
            "   (:searchBy = 'TITLE' AND p.title LIKE %:q%) OR " +
            "   (:searchBy = 'CONTENT' AND p.content LIKE %:q%) OR " +
            "   (:searchBy = 'TITLE_CONTENT' AND (p.title LIKE %:q% OR p.content LIKE %:q%)) OR " +
            "   (:searchBy = 'AUTHOR' AND (u.nickname LIKE %:q% OR u.handle LIKE %:q%)) " +
            ") " +
            "ORDER BY p.postId DESC")
    List<PostEntity> findPostList(
            @Param("cursorId") Long cursorId,
            @Param("category") Category category,
            @Param("searchBy") String searchBy,
            @Param("q") String q,
            Pageable pageable
    );
}