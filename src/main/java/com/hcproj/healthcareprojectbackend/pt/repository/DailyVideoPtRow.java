package com.hcproj.healthcareprojectbackend.pt.repository;

import com.hcproj.healthcareprojectbackend.pt.entity.PtRoomStatus;

import java.time.Instant;

public interface DailyVideoPtRow {
    Long getPtRoomId();
    Instant getScheduledStartAt();
    PtRoomStatus getRoomStatus();
    String getTitle();
}