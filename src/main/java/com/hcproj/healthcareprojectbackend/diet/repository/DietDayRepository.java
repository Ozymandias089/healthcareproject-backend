package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface DietDayRepository extends JpaRepository<DietDayEntity, Long> {
    Optional<DietDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    //특정 기간의 식단 기록 조회 (주간 캘린더용)
    List<DietDayEntity> findAllByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
