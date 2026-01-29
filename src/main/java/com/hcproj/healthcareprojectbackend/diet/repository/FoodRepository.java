package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 음식 마스터({@link FoodEntity})에 대한 영속성 접근 인터페이스.
 */
public interface FoodRepository extends JpaRepository<FoodEntity, Long> {

    /** 활성 음식 전체 목록을 조회한다. */
    List<FoodEntity> findAllByIsActiveTrue();

    /**
     * 활성화된 음식 단건 조회 (상세 조회용).
     */
    Optional<FoodEntity> findByFoodIdAndIsActiveTrue(Long foodId);

    // ============================================================
    // 커서 기반 음식 목록 조회 - 검색어 없음
    // ============================================================
    @Query(value = "SELECT * FROM foods f " +
            "WHERE f.is_active = true " +
            "AND (:cursor IS NULL OR f.food_id > :cursor) " +
            "ORDER BY f.food_id ASC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<FoodEntity> findFoodsWithCursorNoKeyword(
            @Param("cursor") Long cursor,
            @Param("limitSize") int limitSize
    );

    // ============================================================
    // 커서 기반 음식 목록 조회 - 검색어 있음 (띄어쓰기 무시)
    // ============================================================
    @Query(value = "SELECT * FROM foods f " +
            "WHERE f.is_active = true " +
            "AND (:cursor IS NULL OR f.food_id > :cursor) " +
            "AND REPLACE(LOWER(f.name), ' ', '') LIKE :keyword " +
            "ORDER BY f.food_id ASC " +
            "FETCH FIRST :limitSize ROWS ONLY",
            nativeQuery = true)
    List<FoodEntity> findFoodsWithCursorAndKeyword(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("limitSize") int limitSize
    );

    /**
     * 특정 알레르기 코드가 포함되지 않은 활성 음식 목록을 조회한다.
     */
    @Query("""
        select f
        from FoodEntity f
        where f.isActive = true
          and (
              f.allergyCodes is null
              or f.allergyCodes = ''
              or lower(f.allergyCodes) not like concat('%', lower(:code), '%')
          )
        """)
    List<FoodEntity> findActiveExcludingAllergyCode(String code, Pageable pageable);

    /** 알레르기 필터 없이 활성 음식만 페이지 단위로 조회한다. */
    List<FoodEntity> findByIsActiveTrue(Pageable pageable);

    /** ID 목록으로 음식들을 일괄 조회한다. */
    List<FoodEntity> findByFoodIdIn(Collection<Long> foodIds);
}