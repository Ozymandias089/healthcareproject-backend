package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    List<FoodEntity> findAllByIsActiveTrue();
}
