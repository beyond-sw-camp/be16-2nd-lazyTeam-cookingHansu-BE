package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.recipe.repository.RecipeRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;

    /**
     * 레시피 공유 게시글 생성
     */
    @Transactional
    public UUID createRecipePost(PostCreateRequestDto requestDto) {
        User currentUser = getCurrentUser();

        // 게시글 엔티티 생성
        Post post = Post.builder()
                .user(currentUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .thumbnailUrl(requestDto.getThumbnailUrl())
                .category(requestDto.getCategory())
                .isOpen(requestDto.getIsOpen() != null ? requestDto.getIsOpen() : true)
                .build();

        Post savedPost = postRepository.save(post);

        log.info("레시피 공유 게시글 작성 완료. 사용자: {}, 게시글 ID: {}", currentUser.getEmail(), savedPost.getId());
        return savedPost.getId();
    }

    /**
     * 레시피 공유 게시글 목록 조회
     */
    public Page<PostResponseDto> getRecipePosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByIsOpenTrueAndDeletedAtIsNull(pageable);

        return posts.map(PostResponseDto::fromEntity);
    }

    /**
     * 레시피 공유 게시글 검색
     */
    public Page<PostResponseDto> searchRecipePosts(String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.findByIsOpenTrueAndDeletedAtIsNullAndTitleContaining(keyword, pageable);

        return posts.map(PostResponseDto::fromEntity);
    }

    /**
     * 사용자별 레시피 공유 게시글 조회 (본인 게시글 - 공개/비공개 모두)
     */
    public Page<PostResponseDto> getMyRecipePosts(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Post> posts = postRepository.findByUserAndDeletedAtIsNull(currentUser, pageable);

        return posts.map(PostResponseDto::fromEntity);
    }

    /**
     * 특정 사용자의 공개 게시글 조회
     */
    public Page<PostResponseDto> getRecipePostsByUser(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));
        
        // 다른 사용자의 게시글은 공개된 것만 조회
        Page<Post> posts = postRepository.findByUserAndIsOpenTrueAndDeletedAtIsNull(user, pageable);

        return posts.map(PostResponseDto::fromEntity);
    }

    /**
     * 카테고리별 레시피 공유 게시글 조회 (공개된 것만)
     */
    public Page<PostResponseDto> getRecipePostsByCategory(CategoryEnum category, Pageable pageable) {
        Page<Post> posts = postRepository.findByCategoryAndIsOpenTrueAndDeletedAtIsNull(category, pageable);

        return posts.map(PostResponseDto::fromEntity);
    }

    /**
     * 레시피 공유 게시글 상세 조회
     */
    public PostResponseDto getRecipePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        // 비공개 게시글 체크
        if (!post.getIsOpen()) {
            throw new IllegalArgumentException("비공개 게시글입니다.");
        }

        // 조회수 증가
        post.incrementViewCount();

        return PostResponseDto.fromEntity(post);
    }

    /**
     * 레시피 공유 게시글 수정
     */
    @Transactional
    public void updateRecipePost(UUID postId, PostCreateRequestDto requestDto) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // 게시글 기본 정보 수정
        post.updatePost(
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getThumbnailUrl(),
                requestDto.getCategory(),
                requestDto.getIsOpen()
        );

        log.info("레시피 공유 게시글 수정 완료. 사용자: {}, 게시글 ID: {}", currentUser.getEmail(), postId);
    }

    /**
     * 레시피 공유 게시글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteRecipePost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // Soft Delete 사용 (기존 Entity 메서드 활용)
        post.softDelete();

        log.info("레시피 공유 게시글 삭제 완료. 사용자: {}, 게시글 ID: {}", currentUser.getEmail(), postId);
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 현재 로그인한 사용자 조회 (테스트용 임시 구현)
     */
    private User getCurrentUser() {
        // TODO: 실제 JWT 인증 구현 후 주석 해제
        /*
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));
        */
        
        // 임시 테스트용 사용자 조회 (이메일: test@test.com)
        return userRepository.findByEmail("test@test.com")
                .orElseGet(() -> {
                    // 테스트 사용자가 없으면 생성
                    User testUser = User.builder()
                            .name("테스트사용자")
                            .email("test@test.com")
                            .nickname("테스터")
                            .password("password123")
                            .profileImageUrl("https://via.placeholder.com/150")
                            .build();
                    return userRepository.save(testUser);
                });
    }

    /**
     * 게시글 조회 (권한 확인 포함)
     */
    private Post getPostByIdAndUser(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 삭제된 게시글 체크
        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        // 소유자 확인
        if (!post.isOwnedBy(user)) {
            throw new IllegalArgumentException("게시글에 대한 권한이 없습니다.");
        }

        return post;
    }
}