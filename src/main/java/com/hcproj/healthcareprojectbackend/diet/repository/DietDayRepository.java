package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DietDayRepository extends JpaRepository<DietDayEntity, Long> {
    Optional<DietDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
}
