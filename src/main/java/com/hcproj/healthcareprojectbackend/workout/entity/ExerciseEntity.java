package com.hcproj.healthcareprojectbackend.workout.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "exercises")
public class ExerciseEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Long exerciseId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "body_part", length = 20)
    private String bodyPart;

    @Column(name = "difficulty", length = 20)
    private String difficulty;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Lob
    @Column(name = "precautions")
    private String precautions;

    @Column(name = "youtube_url", length = 2048)
    private String youtubeUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
