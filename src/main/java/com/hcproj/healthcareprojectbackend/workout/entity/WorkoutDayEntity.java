package com.hcproj.healthcareprojectbackend.workout.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "workout_days", uniqueConstraints = {
        @UniqueConstraint(name = "uk_workout_days_user_date", columnNames = {"user_id", "log_date"})
})
public class WorkoutDayEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_day_id")
    private Long workoutDayId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Lob
    @Column(name = "title", nullable = false)
    private String title;
}
