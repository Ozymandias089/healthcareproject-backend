package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietMealItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 식단 음식 항목({@link DietMealItemEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>끼니(dietMealId)에 속한 항목 목록 조회</li>
 *   <li>여러 끼니에 대한 항목을 일괄 조회/삭제(배치 처리)</li>
 * </ul>
 */
public interface DietMealItemRepository extends JpaRepository<DietMealItemEntity, Long> {
    /** 특정 끼니의 음식 항목들을 조회한다. */
    List<DietMealItemEntity> findAllByDietMealId(Long dietMealId);

    /** 여러 끼니에 속한 음식 항목들을 일괄 조회한다. */
    List<DietMealItemEntity> findAllByDietMealIdIn(List<Long> dietMealIds);

    /** 여러 끼니에 속한 음식 항목들을 일괄 삭제한다. */
    void deleteByDietMealIdIn(Collection<Long> dietMealIds);
}
