package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId; // users 1:1, PK=FK

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "experience_level", length = 20)
    private String experienceLevel;

    @Column(name = "goal_type", length = 20)
    private String goalType;

    @Column(name = "weekly_days")
    private Integer weeklyDays;

    @Column(name = "session_minutes")
    private Integer sessionMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_allergies")
    private List<AllergyType> allergies;
}