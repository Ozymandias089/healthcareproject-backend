package com.hcproj.healthcareprojectbackend.diet.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "diet_days", uniqueConstraints = {
        @UniqueConstraint(name = "uk_diet_days_user_date", columnNames = {"user_id", "log_date"})
})
public class DietDayEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_day_id")
    private Long dietDayId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
}
