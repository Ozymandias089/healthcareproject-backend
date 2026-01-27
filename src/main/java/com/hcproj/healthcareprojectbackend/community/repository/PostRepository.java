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

    /**
     * 공지사항 목록을 조회한다.
     *
     * @param status 조회할 게시글 상태(예: POSTED)
     * @return 공지글 목록 (postId 내림차순)
     */
    List<PostEntity> findByIsNoticeTrueAndStatusOrderByPostIdDesc(PostStatus status);

    // ========================================================================
    //  커서 기반 페이징 (User Side) - 검색 조건별 메서드 분리
    // ========================================================================

    /**
     * 1. 검색어 없는 기본 목록 조회.
     *
     * @param cursorId 커서 기준 postId (null이면 최신순)
     * @param category 카테고리 (null이면 전체)
     * @param pageable 페이징 정보 (limit)
     * @return 게시글 목록
     */
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.status = 'POSTED' " +
            "ORDER BY p.postId DESC")
    List<PostEntity> findPostList(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            Pageable pageable
    );

    /**
     * 2. 제목(Title) 검색.
     */
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.status = 'POSTED' " +
            "AND p.title LIKE %:q% " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByTitle(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * 3. 내용(Content) 검색.
     */
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.status = 'POSTED' " +
            "AND p.content LIKE %:q% " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByContent(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * 4. 작성자(Author) 검색.
     * <p>UserEntity와 조인하여 닉네임을 검색한다.</p>
     */
    @Query("SELECT p FROM PostEntity p " +
            "LEFT JOIN UserEntity u ON p.userId = u.id " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.status = 'POSTED' " +
            "AND u.nickname LIKE %:q% " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByAuthor(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    /**
     * 5. 제목 + 내용(Title + Content) 검색.
     */
    @Query("SELECT p FROM PostEntity p " +
            "WHERE (:cursorId IS NULL OR p.postId < :cursorId) " +
            "AND (:category IS NULL OR p.category = :category) " +
            "AND p.status = 'POSTED' " +
            "AND (p.title LIKE %:q% OR p.content LIKE %:q%) " +
            "ORDER BY p.postId DESC")
    List<PostEntity> searchByTitleAndContent(
            @Param("cursorId") Long cursorId,
            @Param("category") String category,
            @Param("q") String q,
            Pageable pageable
    );

    // ========================================================================
    //  관리자 / 통계 (Admin Side)
    // ========================================================================

    /** 상태별 게시글 수를 반환한다(관리자 통계용). */
    long countByStatus(PostStatus status);

    /** 특정 시각 이후 작성된 게시글 수를 반환한다(관리자 통계용). */
    long countByCreatedAtAfter(Instant startOfDay);

    /**
     * 관리자용 게시글 목록 조회 (페이지네이션 + 필터링).
     *
     * <p>
     * category/status/keyword 조건이 모두 선택적으로 적용된다.
     * keyword는 제목 또는 작성자 닉네임에 대해 LIKE 검색한다.
     * </p>
     */
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
     * 관리자용 게시글 전체 개수 조회 (필터링 적용).
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