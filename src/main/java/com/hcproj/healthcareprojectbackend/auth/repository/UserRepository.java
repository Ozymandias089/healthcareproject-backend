package com.hcproj.healthcareprojectbackend.auth.repository;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.time.Instant;
import java.util.Optional;

/**
 * 사용자({@link UserEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>회원가입 시 이메일/핸들 중복 체크</li>
 *   <li>로그인/인증 시 이메일/핸들 기반 사용자 조회</li>
 *   <li>관리자 대시보드용 통계 및 필터 조회</li>
 * </ul>
 *
 * <p>
 * createdAt 기반 집계는 {@link com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity}를 전제로 한다.
 * </p>
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    /** 이메일 중복 여부를 확인한다. */
    boolean existsByEmail(String email);
    /** 핸들 중복 여부를 확인한다. */
    boolean existsByHandle(String handle);
    /** 이메일로 사용자를 조회한다. */
    Optional<UserEntity> findByEmail(String email);
    /** 핸들로 사용자를 조회한다. */
    Optional<UserEntity> findByHandle(String handle);

    boolean existsByRole(UserRole role);

    /**
     * 특정 시각 이후 가입(생성)된 사용자 수를 반환한다.
     *
     * <p>
     * 예: "오늘 가입한 회원 수" 집계를 위해 startOfDay(오늘 00:00) 기준으로 사용.
     * </p>
     *
     * @param startOfDay 기준 시각(포함되지 않는 이전 데이터는 제외)
     */
    long countByCreatedAtAfter(Instant startOfDay);

    /**
     * 특정 상태의 사용자 수를 반환한다.
     *
     * @param status 사용자 상태
     */
    long countByStatus(UserStatus status);

    /**
     * 관리자용 사용자 목록 조회 (필터 + 페이지네이션).
     *
     * <p><b>필터 규칙</b></p>
     * <ul>
     *   <li>role이 null이면 전체 role</li>
     *   <li>keyword가 null이면 검색 조건 미적용</li>
     *   <li>keyword는 nickname/email에 대해 LIKE 검색</li>
     * </ul>
     *
     * @param role     필터링할 역할(선택)
     * @param keyword  검색 키워드(선택)
     * @param pageable 페이지 정보
     * @return 조건에 맞는 사용자 페이지
     */
    @Query("SELECT u FROM UserEntity u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "AND (:keyword IS NULL OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%)")
    Page<UserEntity> findAllWithFilters(
            @Param("role") UserRole role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ============================================================
    // 관리자용 사용자 목록 조회 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT * FROM users u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "ORDER BY u.created_at DESC " +
            "OFFSET :offsetSize ROWS FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<UserEntity> findAllWithFiltersNoKeyword(
            @Param("role") String role,
            @Param("limitSize") int limitSize,
            @Param("offsetSize") int offsetSize
    );

    // ============================================================
    // 관리자용 사용자 목록 조회 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM users u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "AND (REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "     OR REPLACE(LOWER(u.email), ' ', '') LIKE :keyword) " +
            "ORDER BY u.created_at DESC " +
            "OFFSET :offsetSize ROWS FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<UserEntity> findAllWithFiltersAndKeyword(
            @Param("role") String role,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize,
            @Param("offsetSize") int offsetSize
    );

    // ============================================================
    // 관리자용 사용자 개수 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT COUNT(*) FROM users u " +
            "WHERE (:role IS NULL OR u.role = :role)",
            nativeQuery = true)
    long countAllWithFiltersNoKeyword(@Param("role") String role);

    // ============================================================
    // 관리자용 사용자 개수 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT COUNT(*) FROM users u " +
            "WHERE (:role IS NULL OR u.role = :role) " +
            "AND (REPLACE(LOWER(u.nickname), ' ', '') LIKE :keyword " +
            "     OR REPLACE(LOWER(u.email), ' ', '') LIKE :keyword)",
            nativeQuery = true)
    long countAllWithFiltersAndKeyword(
            @Param("role") String role,
            @Param("keyword") String keyword
    );
}
