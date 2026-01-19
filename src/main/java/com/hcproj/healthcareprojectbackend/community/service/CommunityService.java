// 파일명: CommunityService.java
package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request) {
        // 1. 작성자 정보 조회 (응답 DTO 구성을 위해 필요)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 게시글 엔티티 생성
        PostEntity post = PostEntity.builder()
                .userId(userId) // 연관관계 없이 ID값만 저장
                .category(request.category())
                .title(request.title())
                .content(request.content()) // Toast UI Markdown 저장 (@Lob이므로 대용량 가능)
                .isNotice(request.isNotice() != null && request.isNotice())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        // 3. 저장
        PostEntity savedPost = postRepository.save(post);

        // 4. 응답 반환
        return PostResponseDTO.from(savedPost, user);
    }
}