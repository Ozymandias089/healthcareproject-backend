package com.hcproj.healthcareprojectbackend.community.repository;

import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * 게시글({@link PostEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 기능</b></p>
 * <ul>
 * <li>공지사항 조회</li>
 * <li>커서 기반 목록 조회 (검색 조건별 분리)</li>
 * <li>관리자용 필터/검색/통계 조회</li>
 * </ul>
 *
 * <p>
 * 일반 목록 조회 쿼리는 상태가 {@code POSTED}인 게시글만 대상으로 한다(삭제글 제외).
 * </p>
 */
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    // ========================================================================
    //  커서 기반 페이징 (User Side) - 검색 조건별 메서드 분리
    // ========================================================================

    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:category IS NULL OR :category = 'ALL' OR p.category = :category) " +
            "AND (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND p.status = :status " +
            "ORDER BY p.postId DESC")
    List<PostEntity> findPostList(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // 2. 제목 검색 (띄어쓰기 무시 로직 적용)
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:category IS NULL OR :category = 'ALL' OR p.category = :category) " +
            "AND (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND REPLACE(p.title, ' ', '') LIKE REPLACE(:q, ' ', '') " +
            "AND p.status = :status " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByTitle(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // 3. 작성자 검색
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:category IS NULL OR :category = 'ALL' OR p.category = :category) " +
            "AND (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND p.userId IN (SELECT u.id FROM UserEntity u WHERE u.nickname LIKE :q) " +
            "AND p.status = :status " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByAuthor(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    // --- 아래는 관리자 기능 (기존 유지하되 CONCAT 문제 방지 위해 LIKE :keyword 사용 권장) ---

    long countByStatus(PostStatus status);
    long countByCreatedAtAfter(Instant startOfDay);

    @Query("SELECT p FROM PostEntity p " +
            "LEFT JOIN UserEntity u ON p.userId = u.id " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:keyword IS NULL OR p.title LIKE :keyword OR u.nickname LIKE :keyword) " +
            "ORDER BY p.postId DESC")
    Page<PostEntity> findAdminPostList(
            @Param("category") String category,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM PostEntity p " +
            "LEFT JOIN UserEntity u ON p.userId = u.id " +
            "WHERE (:category IS NULL OR p.category = :category) " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:keyword IS NULL OR p.title LIKE :keyword OR u.nickname LIKE :keyword)")
    long countAdminPostList(
            @Param("category") String category,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword
    );
}