package com.hcproj.healthcareprojectbackend.diet.entity;

import jakarta.persistence.*;
import lombok.*;

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
