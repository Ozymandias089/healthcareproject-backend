package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * 사용자 프로필(신체 정보/목표/선호)을 저장하는 1:1 확장 엔티티.
 *
 * <p><b>관계 모델</b></p>
 * <ul>
 *   <li>{@code users} 테이블과 1:1 관계</li>
 *   <li>PK = FK 구조로 {@code user_id}가 곧 사용자 식별자</li>
 * </ul>
 *
 * <p><b>저장 정보</b></p>
 * <ul>
 *   <li>기본 신체 정보(키/몸무게/나이/성별)</li>
 *   <li>운동 경험치/목표/주간 운동 빈도/회당 운동 시간</li>
 *   <li>알레르기 목록(컬렉션 테이블로 별도 관리)</li>
 * </ul>
 *
 * <p><b>알레르기 모델링</b></p>
 * <ul>
 *   <li>{@link ElementCollection} + 별도 테이블({@code user_profile_allergies})에 저장</li>
 *   <li>조회는 LAZY로 수행</li>
 * </ul>
 */
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

    /**
     * 알레르기 목록.
     *
     * <p>
     * 별도의 컬렉션 테이블({@code user_profile_allergies})에 저장된다.
     * </p>
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_profile_allergies",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "allergy", length = 30, nullable = false)
    private List<AllergyType> allergies;

    /**
     * 알레르기 목록을 교체한다.
     *
     * <p>
     * 기존 목록을 모두 비운 뒤 새 목록을 추가하는 방식이며,
     * {@code newAllergies}가 null/empty면 결과는 빈 목록이 된다.
     * </p>
     *
     * <p><b>주의</b></p>
     * <ul>
     *   <li>allergies 필드는 null이 아닌 컬렉션으로 초기화되어 있어야 한다.</li>
     *   <li>JPA 컬렉션 변경 감지(더티 체킹)를 활용하기 위해 clear/addAll 방식을 사용한다.</li>
     * </ul>
     *
     * @param newAllergies 새 알레르기 목록
     */
    public void changeAllergies(List<AllergyType> newAllergies) {
        this.allergies.clear();
        if (newAllergies != null && !newAllergies.isEmpty()) {
            this.allergies.addAll(newAllergies);
        }
    }
}