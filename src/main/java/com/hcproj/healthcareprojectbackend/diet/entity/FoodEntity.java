package com.hcproj.healthcareprojectbackend.diet.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 음식(식품) 정보를 나타내는 엔티티.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>식단 기록에서 참조되는 기준 음식 데이터</li>
 *   <li>영양 정보 및 표시 단위 관리</li>
 * </ul>
 *
 * <p><b>영양 정보</b></p>
 * <ul>
 *   <li>탄수화물, 단백질, 지방은 {@link BigDecimal}로 저장하여 정밀도 유지</li>
 *   <li>칼로리는 정수 값으로 관리</li>
 * </ul>
 *
 * <p><b>표시/운영 정책</b></p>
 * <ul>
 *   <li>{@code displayServing}: 사용자에게 보여줄 서빙 기준 설명</li>
 *   <li>{@code allergies}: 알레르기 코드 목록(CSV 형태)</li>
 *   <li>{@code isActive}: 비활성 음식은 선택/검색 대상에서 제외 가능</li>
 * </ul>
 */
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
