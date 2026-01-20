package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponse;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.PostService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 1. 게시글 목록 조회
    @GetMapping
    public ApiResponse<PostListResponse> getPostList(
            @RequestParam(name = "category", required = false, defaultValue = "ALL") String category,
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        return ApiResponse.ok(postService.getPostList(category, searchBy, q, cursorId, size));
    }

    // 2. 게시글 상세 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO> getPostDetail(
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(postService.getPostDetail(postId));
    }

    // 3. 게시글 작성
    @PostMapping
    public ApiResponse<PostResponseDTO> createPost(
            @CurrentUserId Long userId,
            @RequestBody PostCreateRequestDTO request
    ) {
        return ApiResponse.created(postService.createPost(userId, request));
    }

    // 4. 게시글 수정
    @PatchMapping("/{postId}")
    public ApiResponse<PostResponseDTO> updatePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequestDTO request
    ) {
        return ApiResponse.ok(postService.updatePost(userId, postId, request));
    }

    // 5. 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<PostDeleteResponseDTO> deletePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(postService.deletePost(userId, postId));
    }
}