package com.hcproj.healthcareprojectbackend.workout.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 사용자의 특정 날짜에 대한 운동 기록(하루 운동)의 루트 엔티티.
 *
 * <p><b>모델링 의도</b></p>
 * <ul>
 *   <li>사용자(userId)는 하루(logDate)에 하나의 운동 기록만 가진다.</li>
 *   <li>하루 운동은 여러 운동 항목({@link WorkoutItemEntity})으로 구성된다.</li>
 * </ul>
 *
 * <p><b>DB 제약</b></p>
 * <ul>
 *   <li>{@code uk_workout_days_user_date}: (user_id, log_date) 유니크</li>
 * </ul>
 *
 * <p>
 * title은 사용자가 지정한 "오늘의 운동" 제목/요약이며,
 * 실제 운동 구성은 하위 엔티티에서 관리된다.
 * </p>
 */
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

    public void replaceTitle(String title) {
        if (title == null || title.isBlank()) return; // 또는 예외
        if (Objects.equals(this.title, title)) return;
        this.title = title;
    }
}
