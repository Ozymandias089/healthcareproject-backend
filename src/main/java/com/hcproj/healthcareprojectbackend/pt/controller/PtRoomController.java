package com.hcproj.healthcareprojectbackend.pt.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomEntryRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomStatusUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomParticipantsResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomStatusResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomQueryService;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomService;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomStatusService;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomParticipantService;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomKickRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomKickResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pt-rooms")
@RequiredArgsConstructor
public class PtRoomController {

    private final PtRoomService ptRoomService;
    private final PtRoomQueryService ptRoomQueryService;
    private final PtRoomStatusService ptRoomStatusService;
    private final PtRoomParticipantService ptRoomParticipantService;

    @PostMapping("/create")
    public ApiResponse<PtRoomDetailResponseDTO> createRoom(
            @CurrentUserId Long userId,
            @Valid @RequestBody PtRoomCreateRequestDTO request) {
        return ApiResponse.created(ptRoomService.createRoom(userId, request));
    }

    @GetMapping
    public ApiResponse<PtRoomListResponseDTO> getPtRoomList(
            @RequestParam(name = "tab", defaultValue = "ALL") String tab,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @CurrentUserId Long userId) {
        return ApiResponse.ok(ptRoomQueryService.getPtRoomList(tab, q, cursorId, size, userId));
    }

    @GetMapping("/{ptRoomId}")
    public ApiResponse<PtRoomDetailResponseDTO> getPtRoomDetail(
            @PathVariable(name = "ptRoomId") Long ptRoomId
    ) {
        return ApiResponse.ok(ptRoomQueryService.getPtRoomDetail(ptRoomId));
    }

    @PostMapping("/{ptRoomId}/join")
    public ApiResponse<Void> joinRoom(
            @PathVariable(name = "ptRoomId") Long ptRoomId,
            @CurrentUserId Long userId,
            @RequestBody(required = false) PtRoomEntryRequestDTO request
    ) {
        PtRoomEntryRequestDTO safeRequest = (request != null) ? request : new PtRoomEntryRequestDTO(null);
        ptRoomService.joinRoom(ptRoomId, userId, safeRequest);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{ptRoomId}/leave")
    public ApiResponse<Void> leaveRoom(
            @PathVariable(name = "ptRoomId") Long ptRoomId,
            @CurrentUserId Long userId
    ) {
        ptRoomService.leaveRoom(ptRoomId, userId);
        return ApiResponse.ok(null);
    }

    // [신규 추가] 방 삭제 API (Hard Delete)
    @DeleteMapping("/{ptRoomId}")
    public ApiResponse<Void> deleteRoom(
            @PathVariable(name = "ptRoomId") Long ptRoomId,
            @CurrentUserId Long userId
    ) {
        ptRoomService.deleteRoom(ptRoomId, userId);
        return ApiResponse.ok(null);
    }

    // 상태 변경 API
    @PatchMapping("/{ptRoomId}/status")
    public ApiResponse<PtRoomStatusResponseDTO> updateRoomStatus(
            @PathVariable(name = "ptRoomId") Long ptRoomId,
            @CurrentUserId Long userId,
            @RequestBody @Valid PtRoomStatusUpdateRequestDTO request
    ) {
        return ApiResponse.ok(ptRoomStatusService.updateStatus(ptRoomId, userId, request));
    }

    // 화상PT 참여 인원 조회
    @GetMapping("/{ptRoomId}/participants")
    public ApiResponse<PtRoomParticipantsResponseDTO> getPtRoomParticipants(
            @PathVariable Long ptRoomId,
            @CurrentUserId Long userId
    ) {
        return ApiResponse.ok(ptRoomQueryService.getPtRoomParticipants(ptRoomId, userId));
    }

    /* 화상PT 사용자 강퇴 */
    @PostMapping("/{ptRoomId}/kick")
    public ApiResponse<PtRoomKickResponseDTO> kickParticipant(
            @PathVariable Long ptRoomId,
            @RequestBody PtRoomKickRequestDTO request,
            @CurrentUserId Long trainerId
    ) {
        return ApiResponse.ok(
                ptRoomParticipantService.kickParticipant(ptRoomId, trainerId, request)
        );
    }
}