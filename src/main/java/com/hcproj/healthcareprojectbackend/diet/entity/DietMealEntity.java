package com.hcproj.healthcareprojectbackend.diet.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 하루 식단({@link DietDayEntity})에 속한 개별 끼니를 나타내는 엔티티.
 *
 * <p><b>모델링 특징</b></p>
 * <ul>
 *   <li>하루 식단은 여러 끼니를 가질 수 있다.</li>
 *   <li>각 끼니는 정렬 순서({@code sortOrder})를 가진다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_diet_meals_day_sort}: (diet_day_id, sort_order) 유니크</li>
 *   <li>같은 하루 내에서 동일한 정렬 순서를 가진 끼니는 존재할 수 없다.</li>
 * </ul>
 *
 * <p>
 * 끼니에 포함된 음식 목록은 {@link DietMealItemEntity}로 관리된다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "diet_meals", uniqueConstraints = {
        @UniqueConstraint(name = "uk_diet_meals_day_sort", columnNames = {"diet_day_id", "sort_order"})
})
public class DietMealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_meal_id")
    private Long dietMealId;

    @Column(name = "diet_meal_title", nullable = false)
    private String title;

    @Column(name = "diet_day_id", nullable = false)
    private Long dietDayId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
