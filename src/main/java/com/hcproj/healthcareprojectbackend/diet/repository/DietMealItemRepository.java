package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietMealItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface DietMealItemRepository extends JpaRepository<DietMealItemEntity, Long> {
    List<DietMealItemEntity> findAllByDietMealId(Long dietMealId);

    List<DietMealItemEntity> findAllByDietMealIdIn(List<Long> dietMealIds);

    void deleteByDietMealIdIn(Collection<Long> dietMealIds);
}
