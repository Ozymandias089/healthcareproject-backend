package com.hcproj.healthcareprojectbackend.community.service;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.request.PostUpdateRequestDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostDetailResponseDTO;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostListResponseDTO;
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
import java.util.Map;
import java.util.stream.Collectors;

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

        PostEntity post = PostEntity.builder()
                .userId(userId)
                .category(request.category())
                .title(request.title())
                .content(request.content())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .isNotice(false)
                .build();

        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostListResponseDTO getPostList(Long cursorId, String category, String searchBy, String q, int size) {
        // [수정] hasNext 여부를 판단하기 위해 요청된 size보다 1개를 더 조회합니다.
        Pageable pageable = PageRequest.of(0, size + 1);
        List<PostEntity> entities;

        if (q == null || q.isBlank()) {
            // 검색어 없으면 전체 목록
            entities = postRepository.findPostList(cursorId, category, pageable);
        } else {
            // 검색어 있으면 'searchBy' 확인
            String type = (searchBy == null) ? "TITLE" : searchBy.toUpperCase(); // 기본값 TITLE

            switch (type) {
                case "NICKNAME", "AUTHOR" -> // 닉네임(작성자) 검색 선택 시
                        entities = postRepository.searchByAuthor(cursorId, category, q, pageable);

                case "TITLE" -> // 제목 검색 선택 시
                        entities = postRepository.searchByTitle(cursorId, category, q, pageable);

                default -> // 그 외(혹시 모를 경우)는 제목 검색으로 통일
                        entities = postRepository.searchByTitle(cursorId, category, q, pageable);
            }
        }

        // 2. [수정] hasNext 계산 (size보다 더 많이 가져왔다면 다음 페이지가 있는 것)
        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities.remove(size); // 확인용으로 가져온 마지막 데이터는 리스트에서 제거
        }

        // 3. 작성자 정보 일괄 조회 (N+1 방지)
        List<Long> userIds = entities.stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // 4. 다음 커서 계산
        Long nextCursorId = entities.isEmpty() ? null : entities.get(entities.size() - 1).getPostId();

        // 5. [수정] PostSummaryDto 매핑 (프론트엔드 요구 필드 포함)
        List<PostSummaryDto> dtos = entities.stream()
                .map(entity -> {
                    UserEntity author = userMap.get(entity.getUserId());
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
                            0L,                   // commentCount (추후 구현)
                            entity.getViewCount(),
                            0L                    // [필수] likeCount (프론트 에러 방지용)
                    );
                })
                .toList();

        // 6. [핵심 수정] 프론트엔드 구조(items, pageInfo)에 맞춰 반환
        return PostListResponseDTO.builder()
                .notices(List.of())        // 공지사항 (일단 빈 리스트)
                .items(dtos)               // [변경] list -> items
                .pageInfo(PostListResponseDTO.PageInfo.builder()
                        .nextCursorId(nextCursorId)
                        .hasNext(hasNext)  // [추가] 다음 페이지 존재 여부
                        .size(size)        // [추가] 페이지 사이즈
                        .build())
                .build();
    }

    @Transactional
    public PostDetailResponseDTO getPostDetail(Long postId, Long currentUserId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        // 작성자 정보 조회 (한 번만 조회하도록 수정)
        UserEntity writer = userRepository.findById(post.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long views = (post.getViewCount() != null) ? post.getViewCount() : 0L;
        boolean isOwner = currentUserId != null && currentUserId.equals(post.getUserId());

        // [핵심 수정] 프론트엔드 상세 조회 구조(author 객체, comments 리스트)에 맞춤
        return PostDetailResponseDTO.builder()
                .postId(post.getPostId())
                .author(PostDetailResponseDTO.AuthorDTO.builder() // [변경] 닉네임 문자열 -> AuthorDTO 객체
                        .userId(writer.getId())
                        .nickname(writer.getNickname())
                        .handle(writer.getHandle())
                        .profileImageUrl(null) // 프로필 이미지 (없으면 null)
                        .build())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(views)
                .commentCount(0L)          // [추가] 댓글 수
                .likeCount(0L)             // [추가] 좋아요 수
                .createdAt(post.getCreatedAt())
                .updatedAt(null)           // [추가] 수정일
                .isOwner(isOwner)
                .comments(List.of())       // [필수] 빈 댓글 리스트 (프론트 맵핑 에러 방지)
                .build();
    }

    @Transactional
    public void updatePost(Long userId, Long postId, PostUpdateRequestDTO request) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }

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