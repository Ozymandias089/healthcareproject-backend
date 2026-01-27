package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.PostService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse; // 기존 패키지 유지
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId; // 기존 어노테이션 유지
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /** 1. 게시글 목록 조회 */
    @GetMapping(produces = "application/json")
    public ApiResponse<PostListResponseDTO> getPostList(
            @RequestParam(name = "category", required = false, defaultValue = "ALL") String category,
            @RequestParam(name = "searchBy", required = false) String searchBy,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size
    ) {
        // [수정] 서비스의 인자 순서(cursorId가 첫 번째)에 맞춰서 전달합니다.
        return ApiResponse.ok(postService.getPostList(cursorId, category, searchBy, q, size));
    }

    /** 2. 게시글 상세 조회 */
    @GetMapping(path = "/{postId}", produces = "application/json")
    public ApiResponse<PostDetailResponseDTO> getPostDetail(@PathVariable("postId") Long postId) {
        // 서비스 리턴 타입인 PostDetailResponseDTO로 제네릭 맞춤
        return ApiResponse.ok(postService.getPostDetail(postId));
    }

    /** 3. 게시글 작성 */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ApiResponse<?> createPost(@CurrentUserId Long userId, @RequestBody PostCreateRequestDTO request) {
        // 서비스가 void이므로 호출 후 null을 반환하여 에러 해결
        postService.createPost(userId, request);
        return ApiResponse.created(null);
    }

    /** 4. 게시글 수정 */
    @PatchMapping("/{postId}")
    public ApiResponse<?> updatePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequestDTO request
    ) {
        postService.updatePost(userId, postId, request);
        return ApiResponse.ok(null);
    }

    /** 5. 게시글 삭제 */
    @DeleteMapping("/{postId}")
    public ApiResponse<?> deletePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId
    ) {
        postService.deletePost(userId, postId);
        return ApiResponse.ok(null);
    }
}