package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.CommentCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponse;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.CommentService;
import com.hcproj.healthcareprojectbackend.community.service.PostService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final PostService postService;       // 게시글 담당 서비스
    private final CommentService commentService; // 댓글 담당 서비스

    // 1. 게시글 목록 조회
    @GetMapping("/posts")
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
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostResponseDTO> getPostDetail(
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(postService.getPostDetail(postId));
    }

    // 3. 게시글 작성
    @PostMapping("/posts")
    public ApiResponse<PostResponseDTO> createPost(
            @CurrentUserId Long userId,
            @RequestBody PostCreateRequestDTO request
    ) {
        return ApiResponse.created(postService.createPost(userId, request));
    }

    // 4. 게시글 수정
    @PatchMapping("/posts/{postId}")
    public ApiResponse<PostResponseDTO> updatePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestBody PostUpdateRequestDTO request
    ) {
        return ApiResponse.ok(postService.updatePost(userId, postId, request));
    }

    // 5. 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    public ApiResponse<PostDeleteResponseDTO> deletePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(postService.deletePost(userId, postId));
    }

    // 6. 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentCreateResponseDTO> createComment(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @RequestBody CommentCreateRequestDTO request
    ) {
        return ApiResponse.created(commentService.createComment(userId, postId, request));
    }

    // 7. 댓글 수정 [추가됨]
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<CommentUpdateResponseDTO> updateComment(
            @CurrentUserId Long userId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentUpdateRequestDTO request
    ) {
        return ApiResponse.ok(commentService.updateComment(userId, commentId, request));
    }

    // 8. 댓글 삭제 [추가됨]
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<CommentDeleteResponseDTO> deleteComment(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId
    ) {
        return ApiResponse.ok(commentService.deleteComment(userId, postId, commentId));
    }
}
