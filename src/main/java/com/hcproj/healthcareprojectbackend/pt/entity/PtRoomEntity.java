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

    @Column(name = "room_type", nullable = false, length = 20)
    private PtRoomType roomType; // LIVE | RESERVED

    @Column(name = "scheduled_start_at")
    private Instant scheduledStartAt;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "entry_code", length = 255)
    private String entryCode;

    @Column(name = "status", nullable = false, length = 20)
    private PtRoomStatus status; // SCHEDULED | JOINED | LEFT | KICKED

    @Column(name = "janus_room_key", length = 255)
    private String janusRoomKey;

    public void start() {
        this.status = PtRoomStatus.LIVE;
        if (this.scheduledStartAt == null) {
            this.scheduledStartAt = Instant.now();
        }
    }

    /* 방 종료 처리 */
    public void end() {
        this.status = PtRoomStatus.ENDED;
    }
}
