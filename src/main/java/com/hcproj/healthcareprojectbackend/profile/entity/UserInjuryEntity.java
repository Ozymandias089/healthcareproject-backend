package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자의 부상/주의 부위 정보를 저장하는 엔티티.
 *
 * <p><b>역할</b></p>
 * <ul>
 *   <li>트레이닝/식단/추천 로직에서 "주의해야 할 부위"를 반영하기 위한 기초 데이터</li>
 *   <li>사용자 한 명이 여러 부상 정보를 가질 수 있는 구조(1:N)</li>
 * </ul>
 *
 * <p><b>조회 최적화</b></p>
 * <ul>
 *   <li>{@code idx_injuries_user}: user_id 기준 조회를 빠르게 하기 위한 인덱스</li>
 * </ul>
 *
 * <p><b>주의</b></p>
 * <ul>
 *   <li>{@link InjuryLevel}의 유효성/허용 범위는 서비스 레이어에서 검증한다.</li>
 * </ul>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "user_injuries", indexes = {
        @Index(name = "idx_injuries_user", columnList = "user_id")
})
public class UserInjuryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "injury_id")
    private Long injuryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "injury_part", nullable = false, length = 100)
    private String injuryPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "injury_level", nullable = false, length = 20)
    private InjuryLevel injuryLevel; // MILD | CAUTION | SEVERE (서비스에서 검증)
}
