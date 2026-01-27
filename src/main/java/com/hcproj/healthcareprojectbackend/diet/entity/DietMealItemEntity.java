package com.hcproj.healthcareprojectbackend.diet.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 끼니({@link DietMealEntity})에 포함된 개별 음식 항목을 나타내는 엔티티.
 *
 * <p><b>모델링 특징</b></p>
 * <ul>
 *   <li>하나의 끼니는 여러 음식 항목을 가질 수 있다.</li>
 *   <li>음식 정보는 {@link FoodEntity}를 참조한다.</li>
 * </ul>
 *
 * <p><b>조회 최적화</b></p>
 * <ul>
 *   <li>{@code idx_diet_items_meal}: diet_meal_id 기준 조회</li>
 *   <li>{@code idx_diet_items_food}: food_id 기준 조회</li>
 * </ul>
 *
 * <p>
 * 체크 여부({@code isChecked})는 사용자의 섭취 완료 여부를 나타낸다.
 * </p>
 */
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

    /**
     * 섭취 체크 상태를 변경한다.
     *
     * <p>
     * 동일한 상태로 변경 요청 시 아무 동작도 하지 않는다(멱등성 보장).
     * </p>
     *
     * @param checked 변경할 체크 상태
     */
    public void updateChecked(Boolean checked) {
        if (this.isChecked.equals(checked)) {
            return;
        }
        this.isChecked = checked;
    }
}
