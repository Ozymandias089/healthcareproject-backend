package com.hcproj.healthcareprojectbackend.diet.repository;

import com.hcproj.healthcareprojectbackend.calendar.dto.internal.DayCountRow;
import com.hcproj.healthcareprojectbackend.diet.entity.DietDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * 식단 일자 루트({@link DietDayEntity})에 대한 영속성 접근 인터페이스.
 *
 * <p><b>주요 사용 시나리오</b></p>
 * <ul>
 *   <li>사용자/날짜 단건 조회(일일 식단)</li>
 *   <li>주간 캘린더/기간 조회를 위한 범위 조회</li>
 * </ul>
 *
 * <p>
 * 식단의 상세 구성(끼니/항목)은 하위 레포지토리(DietMeal, DietMealItem)에서 조회한다.
 * </p>
 */
public interface DietDayRepository extends JpaRepository<DietDayEntity, Long> {
    /** 사용자/날짜로 식단 루트를 조회한다. */
    Optional<DietDayEntity> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    /**
     * 특정 기간의 식단 기록을 조회한다.
     *
     * <p>
     * 주간 캘린더 등에서 "기록이 있는 날"을 표시하거나 목록을 구성할 때 사용한다.
     * </p>
     *
     * @param userId 사용자 ID
     * @param startDate 시작일(포함)
     * @param endDate 종료일(포함)
     */
    List<DietDayEntity> findAllByUserIdAndLogDateBetween(Long userId, LocalDate startDate, LocalDate endDate);


    interface DayCountView {
        LocalDate getDate();
        Long getPlannedCount();
        Long getDoneCount();
    }

    @Query(value = """
    SELECT
        dd.log_date AS date,
        COUNT(dmi.diet_meal_item_id) AS plannedCount,
        SUM(CASE WHEN dmi.is_checked = TRUE THEN 1 ELSE 0 END) AS doneCount
    FROM diet_days dd
    JOIN diet_meals dm
      ON dm.diet_day_id = dd.diet_day_id
    JOIN diet_meal_items dmi
      ON dmi.diet_meal_id = dm.diet_meal_id
    WHERE dd.user_id = :userId
      AND dd.log_date BETWEEN :startDate AND :endDate
    GROUP BY dd.log_date
    """, nativeQuery = true)
    List<DayCountView> findDietItemCountsGroupedByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
