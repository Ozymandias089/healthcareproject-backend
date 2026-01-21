package com.hcproj.healthcareprojectbackend.pt.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtReservationCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtReservationListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtReservationResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.service.PtReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pt-rooms") // 사용자 요청 URL 반영
@RequiredArgsConstructor
public class PtReservationController {

    private final PtReservationService ptReservationService;

    /* 화상PT 예약 생성 */
    @PostMapping("/{ptRoomId}/reservations")
    public ApiResponse<PtReservationResponseDTO> createReservation(
            @PathVariable Long ptRoomId,
            @RequestBody(required = false) PtReservationCreateRequestDTO request,
            @CurrentUserId Long userId
    ) {
        // request body가 없을 경우 빈 객체로 처리 (공개방 예약 시 편의성)
        PtReservationCreateRequestDTO safeRequest = (request != null) ? request : new PtReservationCreateRequestDTO(null);

        return ApiResponse.created(
                ptReservationService.createReservation(ptRoomId, userId, safeRequest)
        );
    }

        /* 화상PT 예약 취소 */
        @PostMapping("/{ptRoomId}/reservations/cancel")
        public ApiResponse<PtReservationResponseDTO> cancelReservation(
                @PathVariable Long ptRoomId,
                @CurrentUserId Long userId
    ) {
            return ApiResponse.ok(
                    ptReservationService.cancelReservation(ptRoomId, userId)
            );
        }

        /* 화상PT 예약 목록 조회 (트레이너 전용) */
        @GetMapping("/{ptRoomId}/reservations")
        public ApiResponse<PtReservationListResponseDTO> getReservationList(
                @PathVariable Long ptRoomId,
                @CurrentUserId Long trainerId
    ) {
        return ApiResponse.ok(
                ptReservationService.getReservations(ptRoomId, trainerId)
        );
    }
  }