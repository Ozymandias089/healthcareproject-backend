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
@Table(name = "pt_rooms")
public class PtRoomEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pt_room_id")
    private Long ptRoomId;

    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private PtRoomType roomType; // LIVE | RESERVED

    @Column(name = "scheduled_start_at")
    private Instant scheduledStartAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "entry_code", length = 255)
    private String entryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PtRoomStatus status; // SCHEDULED | JOINED | LEFT | KICKED

    public void start() {
        if (this.status == PtRoomStatus.LIVE) {
            return;
        }
        this.status = PtRoomStatus.LIVE;
        if (this.startedAt == null) {
            this.startedAt = Instant.now();
        }
    }

    /* 방 종료 처리 */
    public void end() {
        if (this.status == PtRoomStatus.ENDED) {
            return;
        }
        this.status = PtRoomStatus.ENDED;
        markDeleted();
    }

    // 방 취소 처리 (상태 변경)
    public void cancel() {
        if (this.status == PtRoomStatus.CANCELLED) return;
        this.status = PtRoomStatus.CANCELLED;
        markDeleted();
    }
    // 관리자 화상 PT 강제 종료 처리
    public void forceClose() {
        if (this.status == PtRoomStatus.FORCE_CLOSED) return;
        this.status = PtRoomStatus.FORCE_CLOSED;
        markDeleted();
    }

}
