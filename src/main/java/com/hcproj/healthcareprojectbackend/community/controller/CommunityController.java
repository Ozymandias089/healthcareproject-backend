// 파일명: CommunityController.java
package com.hcproj.healthcareprojectbackend.community.controller;

import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.service.CommunityService;
import com.hcproj.healthcareprojectbackend.global.annotation.CurrentUserId;
import com.hcproj.healthcareprojectbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class CommunityController {

    private final CommunityService communityService;

    // [게시글 생성]
    // Toast UI Editor의 내용을 포함한 요청을 처리합니다.
    @PostMapping("/post")
    public ApiResponse<PostResponseDTO> createPost(
            @CurrentUserId Long userId,
            @Valid @RequestBody PostCreateRequestDTO request
    ) {
        return ApiResponse.ok(communityService.createPost(userId, request));
    }
}