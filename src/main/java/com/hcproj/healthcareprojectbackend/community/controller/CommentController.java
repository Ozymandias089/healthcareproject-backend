package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.CommentCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.CommentUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentCreateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentDeleteResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.CommentUpdateResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.CommentService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentCreateResponseDTO> createComment(
            @CurrentUserId Long userId,
            @PathVariable Long postId,
            @RequestBody CommentCreateRequestDTO request
    ) {
        return ApiResponse.ok(commentService.createComment(userId, postId, request));
    }

    // 2. 댓글 수정
    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<CommentUpdateResponseDTO> updateComment(
            @CurrentUserId Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDTO request
    ) {
        return ApiResponse.ok(commentService.updateComment(userId, postId, commentId, request));
    }

    // 3. 댓글 삭제
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ApiResponse<CommentDeleteResponseDTO> deleteComment(
            @CurrentUserId Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        return ApiResponse.ok(commentService.deleteComment(userId, postId, commentId));
    }
}
