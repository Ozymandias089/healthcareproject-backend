package com.hcproj.healthcareprojectbackend.pt.dto.request;

public record PtReservationCreateRequestDTO(
        String entryCode // Private 방일 경우 필수
) {}