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

    /* 방 입장 처리 */
    public void join() {
        if (this.status == PtParticipantStatus.JOINED) return;

        this.status = PtParticipantStatus.JOINED;
        this.joinedAt = Instant.now();
        this.leftAt = null; // 재입장일 경우 퇴장 시간 초기화
    }

    /* 방 퇴장 처리  */
    public void exit() {
        if (this.status == PtParticipantStatus.LEFT || this.status == PtParticipantStatus.CANCELLED) return;

        this.status = PtParticipantStatus.LEFT;
        this.leftAt = Instant.now(); // 퇴장 시간 기록
    }

    /* 사용자 강퇴 처리  */
    public void kick() {
        if (this.status == PtParticipantStatus.KICKED || this.status == PtParticipantStatus.LEFT) return;

        this.status = PtParticipantStatus.KICKED;
        this.leftAt = Instant.now();
    }
}
