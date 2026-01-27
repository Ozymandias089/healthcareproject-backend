package com.hcproj.healthcareprojectbackend.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 하루 운동({@link WorkoutDayEntity})을 구성하는 개별 운동 항목 엔티티.
 *
 * <p><b>모델링 특징</b></p>
 * <ul>
 *   <li>특정 운동({@code exerciseId})을 참조한다. (마스터: {@link ExerciseEntity})</li>
 *   <li>정렬 순서({@code sortOrder})를 통해 하루 운동 내 표시 순서를 유지한다.</li>
 *   <li>세트/횟수 기반 운동과 시간/거리 기반 운동을 모두 수용하도록 다양한 입력 필드를 가진다.</li>
 * </ul>
 *
 * <p><b>조회 최적화</b></p>
 * <ul>
 *   <li>{@code idx_workout_items_day_sort}: (workout_day_id, sort_order) 인덱스</li>
 *   <li>{@code idx_workout_items_exercise}: exercise_id 인덱스</li>
 * </ul>
 *
 * <p><b>체크 정책</b></p>
 * <ul>
 *   <li>{@code isChecked}는 사용자의 수행 완료 여부를 나타낸다.</li>
 *   <li>{@link #updateChecked(Boolean)}는 동일 값 요청 시 무시하는 멱등(idempotent) 동작을 가진다.</li>
 * </ul>
 *
 * <p>
 * amount는 자유 형식 텍스트(예: "벤치 60kg", "덤벨 12kg") 등을 담기 위한 확장 필드로 사용 가능하다.
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "workout_items", indexes = {
        @Index(name = "idx_workout_items_day_sort", columnList = "workout_day_id,sort_order"),
        @Index(name = "idx_workout_items_exercise", columnList = "exercise_id")
})
public class WorkoutItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_item_id")
    private Long workoutItemId;

    @Column(name = "workout_day_id", nullable = false)
    private Long workoutDayId;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "sets")
    private Integer sets;

    @Column(name = "reps")
    private Integer reps;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "rest_second")
    private Integer restSecond;

    @Column(name = "rpe")
    private Integer rpe;

    @Lob
    @Column(name = "amount")
    private String amount;

    @Column(name = "is_checked", nullable = false)
    private Boolean isChecked;

    /**
     * 수행 체크 상태를 변경한다.
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
