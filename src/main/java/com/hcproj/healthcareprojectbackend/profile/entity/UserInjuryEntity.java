package com.hcproj.healthcareprojectbackend.profile.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "injury_level", nullable = false, length = 20)
    private InjuryLevel injuryLevel; // MILD | CAUTION | SEVERE (서비스에서 검증)
}
