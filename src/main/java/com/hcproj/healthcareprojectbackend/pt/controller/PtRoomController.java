package com.hcproj.healthcareprojectbackend.pt.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomQueryService;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pt-rooms")
@RequiredArgsConstructor
public class PtRoomController {

    private final PtRoomService ptRoomService;
    private final PtRoomQueryService ptRoomqueryService;

    // 1. 방 생성 (Command)
    @PostMapping("/create")
    public ApiResponse<PtRoomCreateResponseDTO> createRoom(
            @CurrentUserId Long userId,
            @Valid @RequestBody PtRoomCreateRequestDTO request) {
        return ApiResponse.created(ptRoomService.createRoom(userId, request));
    }

    // 2. 목록 조회 및 검색 (Query)
    @GetMapping
    public ApiResponse<PtRoomListResponseDTO> getPtRoomList(
            @RequestParam(name = "tab", defaultValue = "ALL") String tab,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @CurrentUserId Long userId) { // (required = false) 제거
        return ApiResponse.ok(ptRoomQueryService.getPtRoomList(tab, q, cursorId, size, userId));
    }
}