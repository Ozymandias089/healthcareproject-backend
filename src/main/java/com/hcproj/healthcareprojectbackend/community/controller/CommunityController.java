package com.hcproj.healthcareprojectbackend.community.controller;

// ▼ [중요] 여기도 .request로 되어 있어야 합니다!
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.CommunityService;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import com.hcproj.healthcareprojectbackend.global.security.annotation.CurrentUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommunityController {

    private final CommunityService communityService;

    // [게시글 생성]
    @PostMapping("/post")
    public ApiResponse<PostResponseDTO> createPost(
            @CurrentUserId Long userId,
            @Valid @RequestBody PostCreateRequestDTO request
    ) {
        return ApiResponse.ok(communityService.createPost(userId, request));
    }

    // [게시글 수정]
    @PutMapping("/post/{postId}")
    public ApiResponse<PostResponseDTO> updatePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequestDTO request
    ) {
        return ApiResponse.ok(communityService.updatePost(userId, postId, request));
    }
    // [게시글 삭제]
    @DeleteMapping("/post/{postId}")
    public ApiResponse<PostDeleteResponseDTO> deletePost(
            @CurrentUserId Long userId,
            @PathVariable("postId") Long postId
    ) {
        return ApiResponse.ok(communityService.deletePost(userId, postId));
    }
}