package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    List<FoodEntity> findAllByIsActiveTrue();
    /**
     * 활성화된 음식 단건 조회 (상세 조회용)
     */
    Optional<FoodEntity> findByFoodIdAndIsActiveTrue(Long foodId);

    /**
     * 음식 리스트 조회 (무한 스크롤, 검색)
     */
    @Query("""
            SELECT f FROM FoodEntity f
            WHERE f.isActive = true
              AND (:cursor IS NULL OR f.foodId > :cursor)
              AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY f.foodId ASC
            LIMIT :limit
            """)
    List<FoodEntity> findFoodsWithCursor(
            @Param("cursor") Long cursor,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    // allergy_codes가 null이거나 비어있거나, 특정 코드가 포함되지 않은 것만
    // Oracle 기준: LOWER + LIKE 조합
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

    // 알레르기 필터 없이 활성만
    List<FoodEntity> findByIsActiveTrue(Pageable pageable);

    List<FoodEntity> findByFoodIdIn(Collection<Long> foodIds);
}
