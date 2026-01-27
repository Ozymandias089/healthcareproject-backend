package com.hcproj.healthcareprojectbackend.diet.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 사용자의 특정 날짜에 대한 식단 기록의 루트 엔티티.
 *
 * <p><b>모델링 의도</b></p>
 * <ul>
 *   <li>사용자(userId)는 하루(logDate)에 하나의 식단 기록만 가진다.</li>
 *   <li>하루 식단은 여러 끼니({@link DietMealEntity})를 포함할 수 있다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_diet_days_user_date}: (user_id, log_date) 유니크</li>
 * </ul>
 *
 * <p>
 * 실제 식단 내용은 이 엔티티에 직접 저장하지 않고,
 * 하위 엔티티(DietMeal → DietMealItem)를 통해 구성된다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "diet_days", uniqueConstraints = {
        @UniqueConstraint(name = "uk_diet_days_user_date", columnNames = {"user_id", "log_date"})
})
public class DietDayEntity extends BaseTimeEntity {

    // 식단 기록 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_day_id")
    private Long dietDayId;

    // 사용자 ID
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 식단 기록 날짜
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
}
