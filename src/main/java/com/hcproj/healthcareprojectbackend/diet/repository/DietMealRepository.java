package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 식단 끼니({@link com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>특정 식단 일자(dietDayId)에 속한 끼니 목록 조회(정렬 포함)</li>
 *   <li>여러 식단 일자에 대한 끼니를 일괄 조회/삭제(배치 처리)</li>
 * </ul>
 *
 * <p>
 * 정렬은 {@code sortOrder ASC}를 기본으로 하여 클라이언트 표시 순서를 보장한다.
 * </p>
 */
public interface DietMealRepository extends JpaRepository<DietMealEntity, Long> {
    /** 특정 식단 일자의 끼니 목록을 sortOrder 오름차순으로 조회한다. */
    List<DietMealEntity> findAllByDietDayIdOrderBySortOrderAsc(Long dietDayId);
    /**
     * 여러 식단 일자에 속한 끼니 목록을 일괄 조회한다.
     *
     * <p>
     * dietDayId ASC, sortOrder ASC로 정렬되어 반환되므로
     * 서비스에서 day별로 그룹핑하기 쉽다.
     * </p>
     */
    List<DietMealEntity> findAllByDietDayIdInOrderByDietDayIdAscSortOrderAsc(List<Long> dietDayIds);
    /** 여러 식단 일자에 속한 끼니 목록을 조회한다(정렬 미보장). */
    List<DietMealEntity> findByDietDayIdIn(Collection<Long> dietDayIds);
    /**
     * 여러 식단 일자에 속한 끼니를 일괄 삭제한다.
     *
     * <p>
     * 자식 엔티티(DietMealItem)의 정합성은 서비스에서 함께 처리하거나
     * DB FK cascade 정책에 따라 달라질 수 있다.
     * </p>
     */
    void deleteByDietDayIdIn(Collection<Long> dietDayIds);
}
