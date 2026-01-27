package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
import java.util.Map;               // Map 클래스를 사용하기 위해 필요
import java.util.stream.Collectors;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostSummaryDto;
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

        // 1. [기존 검색 로직] 검색어와 필터에 따라 엔티티 리스트 조회
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

        // 2. [추가된 로직] 조회된 게시글들의 작성자 정보를 한꺼번에 조회 (N+1 방지)
        List<Long> userIds = entities.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();

        Map<Long, com.hcproj.healthcareprojectbackend.auth.entity.UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(com.hcproj.healthcareprojectbackend.auth.entity.UserEntity::getId, u -> u));

        // 3. 다음 페이지를 위한 커서 ID 계산
        Long nextCursorId = entities.isEmpty() ? null : entities.get(entities.size() - 1).getPostId();

        // 4. [프론트엔드 요구사항] PostSummaryDto 리스트로 변환 (닉네임, 핸들 포함)
        List<PostSummaryDto> list = entities.stream()
                .map(entity -> {
                    var author = userMap.get(entity.getUserId());
                    String nickname = (author != null) ? author.getNickname() : "알 수 없음";
                    String handle = (author != null) ? author.getHandle() : "";

                    return new PostSummaryDto(
                            entity.getPostId(),
                            entity.getCategory(),
                            entity.getIsNotice(),
                            entity.getTitle(),
                            nickname,
                            handle,
                            entity.getCreatedAt(),
                            0L,                   // commentCount
                            entity.getViewCount(),
                            0L                    // [추가] likeCount (일단 0L로 전달, 나중에 로직 구현)
                    );
                })
                .toList();

        return PostListResponseDTO.builder()
                .list(list)
                .nextCursorId(nextCursorId)
                .build();
    }

    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) { // currentUserId 추가
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        String writerNickname = userRepository.findById(post.getUserId())
                .map(UserEntity::getNickname) //
                .orElse("알 수 없음");

        long views = (post.getViewCount() != null) ? post.getViewCount() : 0L;

        // 본인 여부 확인 로직 적용
        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUserId());

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
                .isOwner(isOwner) // 수정된 변수 적용
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