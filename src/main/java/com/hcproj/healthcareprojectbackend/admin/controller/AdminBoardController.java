package com.hcproj.healthcareprojectbackend.admin.controller;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminNoticeCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPostListResponseDTO;
import com.hcproj.healthcareprojectbackend.admin.service.AdminBoardService;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.AdminOnly;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 게시판 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {

    private final AdminBoardService adminBoardService;

    /**
     * 관리자 게시판 통합 조회 API
     * GET /api/admin/board
     *
     * @param page     페이지 번호 (기본값: 0)
     * @param size     한 페이지당 개수 (기본값: 10)
     * @param category 카테고리 필터 (자유, 질문, 정보)
     * @param status   게시글 상태 필터 (POSTED, DELETED)
     * @param keyword  제목 또는 작성자 검색어
     * @return 게시글 목록
     */
    @AdminOnly
    @GetMapping(produces = "application/json")
    public ApiResponse<AdminPostListResponseDTO> getAdminPostList(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return ApiResponse.ok(adminBoardService.getAdminPostList(page, size, category, status, keyword));
    }

    /**
     * 공지사항 등록 API
     * POST /api/admin/board/notice
     *
     * @param adminUserId 관리자 사용자 ID (토큰에서 추출)
     * @param request     공지사항 등록 요청 DTO
     * @return 생성된 게시글 정보
     */
    @AdminOnly
    @PostMapping(path = "/notice", consumes = "application/json", produces = "application/json")
    public ApiResponse<PostResponseDTO> createNotice(
            @CurrentUserId Long adminUserId,
            @Valid @RequestBody AdminNoticeCreateRequestDTO request
    ) {
        return ApiResponse.created(adminBoardService.createNotice(adminUserId, request));
    }
}