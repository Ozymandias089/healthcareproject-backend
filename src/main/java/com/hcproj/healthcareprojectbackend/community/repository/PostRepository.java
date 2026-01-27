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
 *   <li>공지사항 조회</li>
 *   <li>커서 기반 목록 조회(피드)</li>
 *   <li>관리자용 필터/검색/통계 조회</li>
 * </ul>
 *
 * <p>
 * 목록 조회 쿼리는 상태가 {@code POSTED}인 게시글만 대상으로 한다(삭제글 제외).
 * </p>
 */
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    /**
     * 공지사항 목록을 조회한다.
     *
     * @param status 조회할 게시글 상태(예: POSTED)
     * @return 공지글 목록 (postId 내림차순)
     */
    List<PostEntity> findByIsNoticeTrueAndStatusOrderByPostIdDesc(com.hcproj.healthcareprojectbackend.community.entity.PostStatus status);

    /**
     * 커서 기반 게시글 목록 조회.
     *
     * <p><b>커서 규칙</b></p>
     * <ul>
     *   <li>cursorId가 null이면 최신부터 조회</li>
     *   <li>cursorId가 있으면 {@code postId < cursorId} 조건으로 "이전 글"을 조회</li>
     * </ul>
     *
     * <p><b>필터/검색</b></p>
     * <ul>
     *   <li>category가 null이면 전체 카테고리</li>
     *   <li>q가 null이면 검색 미적용, 아니면 title/content LIKE 검색</li>
     *   <li>status는 POSTED만 포함(삭제/비공개 제외 목적)</li>
     * </ul>
     *
     * <p><b>정렬</b></p>
     * postId 내림차순(최신 우선)
     *
     * @param cursorId  커서 기준 postId(선택)
     * @param category  카테고리(선택)
     * @param q         검색어(선택)
     * @param pageable  조회 크기 제한(페이지 크기만 사용)
     * @return 게시글 목록
     */
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
     *
     * <p>
     * {@link #findAdminPostList(String, PostStatus, String, Pageable)}와 동일한 조건을 적용한다.
     * </p>
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