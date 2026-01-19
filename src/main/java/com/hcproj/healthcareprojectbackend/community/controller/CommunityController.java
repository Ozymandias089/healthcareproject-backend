package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponse;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.CommunityService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 1. 게시글 목록 조회
    @GetMapping("/posts")
    public ApiResponse<PostListResponse> getPostList(
            @RequestParam(name = "category", required = false, defaultValue = "ALL") String category,
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        return ApiResponse.ok(communityService.getPostList(category, searchBy, q, cursorId, size));
    }

    // 2. 게시글 상세 조회 (새로 추가됨!)
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostResponseDTO> getPostDetail(
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(communityService.getPostDetail(postId));
    }

    // 3. 게시글 작성
    @PostMapping("/posts")
    public ApiResponse<PostResponseDTO> createPost(
            @RequestBody PostCreateRequestDTO request
    ) {
        // [임시] 로그인 기능 미구현 -> 1번 유저로 고정
        Long userId = 1L;
        return ApiResponse.ok(communityService.createPost(userId, request));
    }

    // 4. 게시글 수정 (PATCH 적용 완료! ✅)
    @PatchMapping("/posts/{postId}")
    public ApiResponse<PostResponseDTO> updatePost(
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequestDTO request
    ) {
        Long userId = 1L; // [임시] 1번 유저
        return ApiResponse.ok(communityService.updatePost(userId, postId, request));
    }

    // 5. 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ApiResponse<PostDeleteResponseDTO> deletePost(
            @PathVariable("postId") Long postId
    ) {
        Long userId = 1L; // [임시] 1번 유저
        return ApiResponse.ok(communityService.deletePost(userId, postId));
    }
}