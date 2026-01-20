package com.hcproj.healthcareprojectbackend.pt.controller;

import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.pt.dto.request.PtRoomCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.dto.response.PtRoomListResponseDTO;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomQueryService;
import com.hcproj.healthcareprojectbackend.pt.service.PtRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pt-rooms")
@RequiredArgsConstructor // 이 어노테이션이 final 필드들을 생성자로 만들어줍니다.
public class PtRoomController {

    private final PtRoomService ptRoomService;
    private final PtRoomQueryService ptRoomQueryService; // 변수명이 정확한지 확인하세요.

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
            @RequestParam(name = "size", defaultValue = "10") int size,
            @CurrentUserId Long userId) {
        // 이 라인에서 ptRoomQueryService 변수 사용
        return ApiResponse.ok(ptRoomQueryService.getPtRoomList(tab, q, cursorId, size, userId));
    }

    @GetMapping("/{ptRoomId}")
    public ApiResponse<PtRoomDetailResponseDTO> getPtRoomDetail(
            @PathVariable(name = "ptRoomId") Long ptRoomId
    ) {
        return ApiResponse.ok(ptRoomQueryService.getPtRoomDetail(ptRoomId));
    }
}