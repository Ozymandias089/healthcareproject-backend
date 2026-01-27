package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 커뮤니티 게시글 관련 비즈니스 로직 서비스.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createPost(Long userId, PostCreateRequestDTO request) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // Entity 구조에 맞춰 생성
        PostEntity post = PostEntity.builder()
                .userId(userId)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .status(PostStatus.POSTED)
                .viewCount(0L) // Long 타입 대응
                .isNotice(false)
                .build();

        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostListResponseDTO getPostList(Long cursorId, String category, String searchBy, String q, int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<PostEntity> entities;

        if (q == null || q.isBlank()) {
            entities = postRepository.findPostList(cursorId, category, pageable);
        } else {
            String type = (searchBy == null) ? "TITLE_CONTENT" : searchBy.toUpperCase();
            switch (type) {
                case "TITLE" -> entities = postRepository.searchByTitle(cursorId, category, q, pageable);
                case "CONTENT" -> entities = postRepository.searchByContent(cursorId, category, q, pageable);
                case "AUTHOR" -> entities = postRepository.searchByAuthor(cursorId, category, q, pageable);
                default -> entities = postRepository.searchByTitleAndContent(cursorId, category, q, pageable);
            }
        }

        Long nextCursorId = entities.isEmpty() ? null : entities.get(entities.size() - 1).getPostId();

        List<PostListResponseDTO.PostSimpleDTO> list = entities.stream()
                .map(entity -> {
                    Long views = (entity.getViewCount() != null) ? entity.getViewCount() : 0L;

                    return PostListResponseDTO.PostSimpleDTO.builder()
                            .postId(entity.getPostId())
                            .category(entity.getCategory())
                            .title(entity.getTitle())
                            .viewCount(views)
                            .commentCount(0L)
                            .likeCount(0L)
                            .createdAt(entity.getCreatedAt())
                            .isNotice(entity.getIsNotice())
                            .build();
                })
                .toList(); // 이제 List<Object> 에러가 사라집니다.

        return PostListResponseDTO.builder()
                .list(list)
                .nextCursorId(nextCursorId)
                .build();
    }

    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        String writerNickname = userRepository.findById(post.getUserId())
                .map(u -> u.getNickname())
                .orElse("알 수 없음");

        long views = (post.getViewCount() != null) ? post.getViewCount() : 0L;

        return PostDetailResponseDTO.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .writerNickname(writerNickname)
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(views)
                .likeCount(0L)
                .createdAt(post.getCreatedAt())
                .isOwner(false)
                .build();
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            // ErrorCode.NOT_POST_AUTHOR (COMMUNITY-002) 사용
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        // [핵심] Entity의 update(String, String, String, Boolean) 4개 인자에 맞춰 호출
        // 수정 시 공지 여부(Boolean)는 기존 값을 유지하도록 post.getIsNotice() 전달
        post.update(
                request.title(),
                request.content(),
                request.category(),
                request.isNotice()
        );
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

        post.delete();
    }
}