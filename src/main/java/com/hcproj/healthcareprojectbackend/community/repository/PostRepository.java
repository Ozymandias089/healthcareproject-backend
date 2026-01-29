package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {

    // ============================================================
    // 1. 전체 목록 조회 (검색어 없음) - 카테고리 있음
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "AND p.category = :category " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListByCategory(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 2. 전체 목록 조회 (검색어 없음) - 카테고리 없음 (전체)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListAll(
            @Param("cursorId") Long cursorId,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 3. 제목 검색 - 카테고리 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "AND p.category = :category " +
            "AND REPLACE(p.title, ' ', '') LIKE :keyword " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListByTitleAndCategory(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 4. 제목 검색 - 카테고리 없음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "AND REPLACE(p.title, ' ', '') LIKE :keyword " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListByTitle(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 5. 작성자 검색 - 카테고리 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "AND p.category = :category " +
            "AND p.user_id IN (SELECT u.user_id FROM users u WHERE REPLACE(u.nickname, ' ', '') LIKE :keyword) " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListByAuthorAndCategory(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 6. 작성자 검색 - 카테고리 없음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.status = 'POSTED' " +
            "AND (:cursorId IS NULL OR p.post_id < :cursorId) " +
            "AND p.user_id IN (SELECT u.user_id FROM users u WHERE REPLACE(u.nickname, ' ', '') LIKE :keyword) " +
            "ORDER BY p.post_id DESC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findPostListByAuthor(
            @Param("cursorId") Long cursorId,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 기존 통계 기능 유지
    // ============================================================
    long countByStatus(PostStatus status);

    long countByCreatedAtAfter(java.time.Instant startOfDay);

    // ============================================================
    // 어드민용 게시글 목록 조회 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "ORDER BY p.created_at DESC " +
            "OFFSET :offsetSize ROWS FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findAdminPostListNoKeyword(
            @Param("category") String category,
            @Param("status") String status,
            @Param("limitSize") int limitSize,
            @Param("offsetSize") int offsetSize
    );

    // ============================================================
    // 어드민용 게시글 목록 조회 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM posts p " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "     OR REPLACE(LOWER(p.content), ' ', '') LIKE :keyword) " +
            "ORDER BY p.created_at DESC " +
            "OFFSET :offsetSize ROWS FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<PostEntity> findAdminPostListWithKeyword(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize,
            @Param("offsetSize") int offsetSize
    );

    // ============================================================
    // 어드민용 게시글 개수 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT COUNT(*) FROM posts p " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status)",
            nativeQuery = true)
    long countAdminPostListNoKeyword(
            @Param("category") String category,
            @Param("status") String status
    );

    // ============================================================
    // 어드민용 게시글 개수 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT COUNT(*) FROM posts p " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (REPLACE(LOWER(p.title), ' ', '') LIKE :keyword " +
            "     OR REPLACE(LOWER(p.content), ' ', '') LIKE :keyword)",
            nativeQuery = true)
    long countAdminPostListWithKeyword(
            @Param("category") String category,
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}