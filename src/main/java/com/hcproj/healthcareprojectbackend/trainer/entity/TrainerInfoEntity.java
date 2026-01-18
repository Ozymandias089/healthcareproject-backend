package com.hcproj.healthcareprojectbackend.trainer.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "trainer_info", indexes = {
        @Index(name = "idx_trainer_status", columnList = "application_status")
})
public class TrainerInfoEntity extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId; // users 1:1, PK=FK

    @Column(name = "application_status", nullable = false, length = 20)
    private TrainerApplicationStatus applicationStatus; // PENDING | APPROVED | REJECTED

    @Lob
    @Column(name = "license_urls_json")
    private String licenseUrlsJson;

    @Lob
    @Column(name = "bio")
    private String bio;

    @Lob
    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
