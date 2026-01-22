package com.hcproj.healthcareprojectbackend.pt.repository;

import java.time.Instant;

public interface DailyVideoPtRow {
    Long getPtRoomId();
    Instant getScheduledStartAt();
    String getTitle();
}