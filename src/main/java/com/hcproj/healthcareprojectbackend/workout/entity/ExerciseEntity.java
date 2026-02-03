package com.hcproj.healthcareprojectbackend.workout.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 운동(Exercise) 마스터 데이터를 나타내는 엔티티.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>사용자가 운동 기록을 작성할 때 참조하는 기준 운동 데이터</li>
 *   <li>운동명, 부위, 난이도, 설명/주의사항, 참고 링크 등을 보관</li>
 * </ul>
 *
 * <p><b>표시/운영 정책</b></p>
 * <ul>
 *   <li>{@code isActive}가 false인 운동은 검색/선택에서 제외할 수 있다.</li>
 *   <li>{@code imageUrl}, {@code youtubeUrl}은 클라이언트 표시용 링크다.</li>
 * </ul>
 *
 * <p>
 * {@code bodyPart}, {@code difficulty}는 현재 문자열로 저장되며,
 * 값의 허용 범위/표준화 정책은 서비스(또는 Enum)로 관리할 수 있다.
 * </p>
 */
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

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "precautions", columnDefinition = "text")
    private String precautions;

    @Column(name = "youtube_url", length = 2048)
    private String youtubeUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
