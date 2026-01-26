package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietMealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DietMealRepository extends JpaRepository<DietMealEntity, Long> {
    List<DietMealEntity> findAllByDietDayIdOrderBySortOrderAsc(Long dietDayId);
    List<DietMealEntity> findAllByDietDayIdInOrderByDietDayIdAscSortOrderAsc(List<Long> dietDayIds);
    List<DietMealEntity> findByDietDayIdIn(Collection<Long> dietDayIds);
    void deleteByDietDayIdIn(Collection<Long> dietDayIds);
}
