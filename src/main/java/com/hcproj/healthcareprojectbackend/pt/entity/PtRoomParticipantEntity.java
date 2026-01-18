package com.hcproj.healthcareprojectbackend.pt.entity;

import com.hcproj.healthcareprojectbackend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "pt_room_participants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pt_room_participant_room_user", columnNames = {"pt_room_id", "user_id"})
}, indexes = {
        @Index(name = "idx_pt_room_participant_user", columnList = "user_id"),
        @Index(name = "idx_pt_room_participant_room_status", columnList = "pt_room_id,status")
})
public class PtRoomParticipantEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_room_participant_id")
    private Long ptRoomParticipantId;

    @Column(name = "pt_room_id", nullable = false)
    private Long ptRoomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false, length = 20)
    private PtParticipantStatus status; // JOINED | LEFT | KICKED

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;
}
