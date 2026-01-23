package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminPtRoomForceCloseRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPtRoomForceCloseResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminPtRoomService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 화상 PT 방 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/pt-rooms")
@RequiredArgsConstructor
public class AdminPtRoomController {

    private final AdminPtRoomService adminPtRoomService;

    /**
     * 관리자 화상 PT 방 목록 조회 API
     * GET /api/admin/pt-rooms
     *
     * @param status        방 상태 필터 (SCHEDULED, LIVE, ENDED, CANCELLED, FORCE_CLOSED)
     * @param trainerHandle 트레이너 핸들 필터
     * @param page          페이지 번호 (기본값: 0)
     * @param size          한 페이지당 개수 (기본값: 10)
     * @return PT 방 목록
     */
    @AdminOnly
    @GetMapping(produces = "application/json")
    public ApiResponse<AdminPtRoomListResponseDTO> getAdminPtRoomList(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "trainerHandle", required = false) String trainerHandle,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "q", required = false) String q
    ) {
        return ApiResponse.ok(adminPtRoomService.getAdminPtRoomList(status, trainerHandle, page, size, q));
    }

    /**
     * 관리자 화상 PT 방 강제 종료 API
     * DELETE /api/admin/pt-rooms/{ptRoomId}
     *
     * @param ptRoomId 종료할 PT 방 ID
     * @param request  강제 종료 요청 DTO (사유 포함)
     * @return 강제 종료 결과
     */
    @AdminOnly
    @DeleteMapping(path = "/{ptRoomId}", consumes = "application/json", produces = "application/json")
    public ApiResponse<AdminPtRoomForceCloseResponseDTO> forceClosePtRoom(
            @PathVariable Long ptRoomId,
            @Valid @RequestBody AdminPtRoomForceCloseRequestDTO request
    ) {
        return ApiResponse.ok(adminPtRoomService.forceClosePtRoom(ptRoomId, request));
    }
}
