package com.hcproj.healthcareprojectbackend.admin.service;

import com.hcproj.healthcareprojectbackend.admin.dto.request.AdminNoticeCreateRequestDTO;
import com.hcproj.healthcareprojectbackend.admin.dto.response.AdminPostListResponseDTO;
import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.community.dto.response.PostResponseDTO;
import com.hcproj.healthcareprojectbackend.community.entity.PostEntity;
import com.hcproj.healthcareprojectbackend.community.entity.PostStatus;
import com.hcproj.healthcareprojectbackend.community.repository.PostRepository;
import com.hcproj.healthcareprojectbackend.global.exception.BusinessException;
import com.hcproj.healthcareprojectbackend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 관리자 게시판 서비스
 */
@Service
@RequiredArgsConstructor
public class AdminBoardService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * 관리자 게시판 통합 조회
     *
     * @param page     페이지 번호 (0부터 시작)
     * @param size     한 페이지당 개수
     * @param category 카테고리 필터 (null이면 전체)
     * @param status   게시글 상태 필터 (null이면 전체)
     * @param keyword  검색어 (제목 또는 작성자)
     * @return 게시글 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public AdminPostListResponseDTO getAdminPostList(
            int page,
            int size,
            String category,
            String status,
            String keyword
    ) {
        // 1) 파라미터 변환
        String categoryParam = (category == null || category.isBlank() || "ALL".equalsIgnoreCase(category))
                ? null : category.toUpperCase();

        PostStatus statusParam = null;
        if (status != null && !status.isBlank()) {
            try {
                statusParam = PostStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 status 값은 무시하고 전체 조회
                statusParam = null;
            }
        }

        String keywordParam = (keyword == null || keyword.isBlank()) ? null : keyword;

        // 2) 페이지네이션 설정
        Pageable pageable = PageRequest.of(page, size);

        // 3) 게시글 조회
        Page<PostEntity> postPage = postRepository.findAdminPostList(
                categoryParam, statusParam, keywordParam, pageable
        );

        // 4) 전체 개수 조회
        long total = postRepository.countAdminPostList(categoryParam, statusParam, keywordParam);

        // 5) 작성자 정보 조회 (N+1 방지를 위해 한 번에 조회)
        List<Long> userIds = postPage.getContent().stream()
                .map(PostEntity::getUserId)
                .distinct()
                .toList();

        Map<Long, UserEntity> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        // 6) 응답 DTO 변환
        List<AdminPostListResponseDTO.AdminPostItemDTO> list = postPage.getContent().stream()
                .map(post -> {
                    UserEntity user = userMap.get(post.getUserId());
                    return AdminPostListResponseDTO.AdminPostItemDTO.builder()
                            .postId(post.getPostId())
                            .author(AdminPostListResponseDTO.AuthorDTO.builder()
                                    .nickname(user != null ? user.getNickname() : "알 수 없음")
                                    .handle(user != null ? user.getHandle() : "unknown")
                                    .build())
                            .category(post.getCategory())
                            .title(post.getTitle())
                            .viewCount(post.getViewCount())
                            .isNotice(post.getIsNotice())
                            .status(post.getStatus())
                            .createdAt(post.getCreatedAt())
                            .build();
                })
                .toList();

        return AdminPostListResponseDTO.builder()
                .total(total)
                .list(list)
                .build();
    }

    /**
     * 공지사항 등록
     *
     * @param adminUserId 관리자 사용자 ID
     * @param request     공지사항 등록 요청 DTO
     * @return 생성된 게시글 응답 DTO
     */
    @Transactional
    public PostResponseDTO createNotice(Long adminUserId, AdminNoticeCreateRequestDTO request) {
        // 1) 관리자 사용자 조회
        UserEntity adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2) 게시글 엔티티 생성
        PostEntity post = PostEntity.builder()
                .userId(adminUserId)
                .category(request.category().toUpperCase())
                .title(request.title())
                .content(request.content())
                .isNotice(request.isNotice())
                .status(PostStatus.POSTED)
                .viewCount(0L)
                .build();

        // 3) 저장
        PostEntity savedPost = postRepository.save(post);

        // 4) 응답 DTO 반환
        return PostResponseDTO.builder()
                .postId(savedPost.getPostId())
                .author(new PostResponseDTO.AuthorDTO(adminUser.getNickname(), adminUser.getHandle()))
                .category(savedPost.getCategory())
                .isNotice(savedPost.getIsNotice())
                .title(savedPost.getTitle())
                .viewCount(savedPost.getViewCount())
                .commentCount(0)
                .content(savedPost.getContent())
                .status(savedPost.getStatus())
                .createdAt(savedPost.getCreatedAt())
                .updatedAt(null)
                .deletedAt(null)
                .comments(Collections.emptyList())
                .build();
    }
}