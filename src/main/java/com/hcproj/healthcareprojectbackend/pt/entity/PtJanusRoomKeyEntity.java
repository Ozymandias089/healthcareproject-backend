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
@Table(name = "pt_janus_room_keys",
        indexes = {
                @Index(name = "idx_pt_janus_keys_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_pt_janus_keys_pt_room", columnNames = {"pt_room_id"})
        }
)
public class PtJanusRoomKeyEntity extends BaseTimeEntity {

    @Id
    @Column(name = "room_key")
    private Integer roomKey; // 30000~39999

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PtJanusKeyStatus status;

    @Column(name = "pt_room_id")
    private Long ptRoomId; // allocated room id (nullable, unique)

    @Column(name = "allocated_at")
    private Instant allocatedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    public boolean isAvailable() {
        return this.status == PtJanusKeyStatus.AVAILABLE && this.ptRoomId == null;
    }

    public void allocateTo(Long ptRoomId) {
        this.status = PtJanusKeyStatus.ALLOCATED;
        this.ptRoomId = ptRoomId;
        this.allocatedAt = Instant.now();
        this.releasedAt = null;
    }

    public void release() {
        this.status = PtJanusKeyStatus.AVAILABLE;
        this.ptRoomId = null;
        this.releasedAt = Instant.now();
        this.allocatedAt = null;
    }
}
