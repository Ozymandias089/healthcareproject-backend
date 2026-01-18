package com.hcproj.healthcareprojectbackend.diet.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "diet_meal_items", indexes = {
        @Index(name = "idx_diet_items_meal", columnList = "diet_meal_id"),
        @Index(name = "idx_diet_items_food", columnList = "food_id")
})
public class DietMealItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_meal_item_id")
    private Long dietMealItemId;

    @Column(name = "diet_meal_id", nullable = false)
    private Long dietMealId;

    @Column(name = "food_id", nullable = false)
    private Long foodId;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked;
}
