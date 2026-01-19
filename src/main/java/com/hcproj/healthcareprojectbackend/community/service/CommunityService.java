package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole; // Role Enum (ADMIN 체크용)
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDeleteResponseDTO;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // [게시글 생성]
    @Transactional
    public PostResponseDTO createPost(Long userId, PostCreateRequestDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PostEntity post = PostEntity.builder()
                .userId(userId)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .isNotice(request.isNotice() != null && request.isNotice())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        PostEntity savedPost = postRepository.save(post);

        return PostResponseDTO.from(savedPost, user);
    }

    // [게시글 수정] - 추가된 메서드
    @Transactional
    public PostResponseDTO updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        // 1. 게시글 조회
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 2. 권한 체크 (작성자 본인 확인)
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        // 3. 내용 수정 (Dirty Checking)
        post.update(request.title(), request.content(), request.category(), request.isNotice());

        // 4. 응답 생성을 위해 유저 정보 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return PostResponseDTO.from(post, user);
    }

        // [게시글 삭제]
        @Transactional
        public PostDeleteResponseDTO deletePost(Long userId, Long postId) {
            // 1. 게시글 조회 (없으면 에러)
            PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

            // 2. 요청자(User) 조회 (권한 체크를 위해 필요)
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 3. 권한 검증: 작성자도 아니고, 관리자(ADMIN)도 아니면 삭제 불가
            boolean isWriter = post.getUserId().equals(userId);
            boolean isAdmin = user.getRole() == UserRole.ADMIN;

            if (!isWriter && !isAdmin) {
                throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
            }

            // 4. 삭제 수행
            postRepository.delete(post);

            // 5. 응답 반환
            return PostDeleteResponseDTO.of(LocalDateTime.now());
        }
    }