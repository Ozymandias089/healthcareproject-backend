package com.hcproj.healthcareprojectbackend.workout.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    // 체크 상태 변경
    public void updateChecked(Boolean checked) {
        this.isChecked = checked;
    }
}
