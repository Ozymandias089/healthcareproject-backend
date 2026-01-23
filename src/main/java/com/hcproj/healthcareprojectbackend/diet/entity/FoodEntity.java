package com.hcproj.healthcareprojectbackend.diet.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "foods")
public class FoodEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "nutrition_unit", nullable = false, length = 20)
    private String nutritionUnit; // G | ML | UNIT | CUP | L

    @Column(name = "nutrition_amount", nullable = false)
    private Integer nutritionAmount;

    @Column(name = "calories", nullable = false)
    private Integer calories;

    @Column(name = "carbs", precision = 10, scale = 2)
    private BigDecimal carbs;

    @Column(name = "protein", precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(name = "fat", precision = 10, scale = 2)
    private BigDecimal fat;

    @Column(name = "display_serving", length = 100)
    private String displayServing;

    @Column(name = "allergy_codes", length = 1000)
    private String allergyCodes; // CSV

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
