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
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>활성 음식 목록 조회</li>
 *   <li>검색/무한 스크롤(커서 기반) 조회</li>
 *   <li>알레르기 코드 제외 필터링 조회</li>
 * </ul>
 *
 * <p>
 * {@code allergyCodes}는 CSV 문자열이며, 정교한 필터링이 필요하면 정규화(별도 테이블)도 고려할 수 있다.
 * </p>
 */
public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    /** 활성 음식 전체 목록을 조회한다. */
    List<FoodEntity> findAllByIsActiveTrue();
    /**
     * 활성화된 음식 단건 조회 (상세 조회용).
     *
     * @param foodId 음식 ID
     * @return 활성 음식이면 Optional로 반환
     */
    Optional<FoodEntity> findByFoodIdAndIsActiveTrue(Long foodId);

    /**
     * 커서 기반 음식 목록 조회(무한 스크롤 + 검색).
     *
     * <p><b>커서 규칙</b></p>
     * <ul>
     *   <li>cursor가 null이면 가장 작은 foodId부터(ASC) 조회</li>
     *   <li>cursor가 있으면 {@code foodId > cursor} 조건으로 다음 페이지를 조회</li>
     * </ul>
     *
     * <p><b>검색 규칙</b></p>
     * <ul>
     *   <li>keyword가 null이면 검색 조건 미적용</li>
     *   <li>keyword가 있으면 name에 대해 대소문자 무시 LIKE 검색</li>
     * </ul>
     *
     * @param cursor  기준 foodId(선택)
     * @param keyword 검색어(선택)
     * @param limit   조회 개수 제한
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

    /**
     * 특정 알레르기 코드가 포함되지 않은 활성 음식 목록을 조회한다.
     *
     * <p>
     * allergyCodes가 null/빈 문자열이거나, CSV 문자열에 code가 포함되지 않은 경우만 반환한다.
     * 매칭은 {@code LOWER + LIKE} 기반의 단순 부분 문자열 비교다.
     * </p>
     *
     * @param code     제외할 알레르기 코드
     * @param pageable 페이지/정렬(조회 개수 제한 포함)
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

    /**
     * 알레르기 필터 없이 활성 음식만 페이지 단위로 조회한다.
     *
     * @param pageable 페이지/정렬(조회 개수 제한 포함)
     */
    List<FoodEntity> findByIsActiveTrue(Pageable pageable);

    /** ID 목록으로 음식들을 일괄 조회한다. */
    List<FoodEntity> findByFoodIdIn(Collection<Long> foodIds);
}
