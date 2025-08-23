package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.dto.PostRecipeStepDto;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.recipe.repository.RecipeRepository;
import lazyteam.cooking_hansu.domain.recipe.repository.RecipeStepRepository;
import lazyteam.cooking_hansu.domain.recipe.repository.PostSequenceDescriptionRepository;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.entity.RecipeStep;
import lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final PostSequenceDescriptionRepository postSequenceDescriptionRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    /**
     * 레시피 공유 게시글 생성 (이미지 포함) - 레시피 연결 지원
     */
    @Transactional
    public UUID createRecipePost(PostCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();

        // 썸네일 업로드
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("게시글 썸네일 업로드 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("게시글 썸네일 업로드 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            // JSON으로 전달된 URL 사용
            thumbnailUrl = requestDto.getThumbnailUrl();
        }
        // 게시글 엔티티 생성
        Post post = Post.builder()
                .user(currentUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .category(requestDto.getCategory())
                .isOpen(requestDto.getIsOpen() != null ? requestDto.getIsOpen() : true)
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    /**
     * 레시피 공유 게시글 생성 (이미지 없음)
     */
    @Transactional
    public UUID createRecipePost(PostCreateRequestDto requestDto) {
        return createRecipePost(requestDto, null);
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
     * 레시피 공유 게시글 상세 조회 (레시피 연결 정보 포함)
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

        // 조회수 증가는 InteractionService에서 처리됨

            return PostResponseDto.fromEntity(post);
    }

    /**
     * 레시피 공유 게시글 수정 (썸네일 포함)
     */
    @Transactional
    public void updateRecipePost(UUID postId, PostCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // 썸네일 업로드 처리
        String thumbnailUrl = post.getThumbnailUrl(); // 기존 URL 유지
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("게시글 썸네일 업데이트 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("게시글 썸네일 업데이트 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            // JSON으로 전달된 URL 사용
            thumbnailUrl = requestDto.getThumbnailUrl();
        }

        // 게시글 기본 정보 수정
        post.updatePost(
                requestDto.getTitle(),
                requestDto.getDescription(),
                thumbnailUrl,
                requestDto.getCategory(),
                requestDto.getIsOpen()
        );

        log.info("레시피 공유 게시글 수정 완료. 사용자: {}, 게시글 ID: {}", currentUser.getEmail(), postId);
    }

    /**
     * 레시피 공유 게시글 수정 (썸네일 없음)
     */
    @Transactional
    public void updateRecipePost(UUID postId, PostCreateRequestDto requestDto) {
        updateRecipePost(postId, requestDto, null);
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
     * 현재 로그인한 사용자 조회 (테스트용 고정 UUID 사용)
     */
    private User getCurrentUser() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        
        return userRepository.findById(testUserId)
                .orElseGet(() -> {
                    // 기본 테스트 사용자가 없으면 생성
                    User testUser = User.builder()
                            .name("기본사용자")
                            .email("default@test.com")
                            .nickname("기본사용자")
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
    
    /**
     * 게시글에 레시피 연결 (조리순서별 설명 포함)
     */
    private void linkRecipeToPost(Post post, UUID recipeId, List<PostRecipeStepDto> stepDescriptions) {
        if (recipeId == null) {
            return;
        }
        
        // 레시피 존재 확인
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("레시피를 찾을 수 없습니다."));

        // 각 조리순서별 설명 생성
        if (stepDescriptions != null && !stepDescriptions.isEmpty()) {
            for (PostRecipeStepDto stepDto : stepDescriptions) {
                // RecipeStep 존재 여부 확인
                RecipeStep recipeStep = recipeStepRepository.findById(stepDto.getStepId())
                        .orElseThrow(() -> new EntityNotFoundException("조리순서를 찾을 수 없습니다."));

                // 해당 단계가 실제로 이 레시피의 것인지 확인
                if (!recipeStep.getRecipe().getId().equals(recipeId)) {
                    throw new IllegalArgumentException("잘못된 조리순서입니다.");
                }

                // 중복 방지
                if (postSequenceDescriptionRepository.existsByPostIdAndRecipeStepId(post.getId(), stepDto.getStepId())) {
                    throw new IllegalArgumentException("동일한 조리순서에 대한 설명이 이미 존재합니다.");
                }
                
                PostSequenceDescription description = PostSequenceDescription.builder()
                        .post(post)
                        .recipeStep(recipeStep)
                        .content(stepDto.getContent())
                        .build();
                
                postSequenceDescriptionRepository.save(description);
            }
            
            log.info("레시피 연결 완료 - postId: {}, recipeId: {}, 설명 개수: {}", 
                    post.getId(), recipeId, stepDescriptions.size());
        }
    }
    
    /**
     * 게시글의 레시피 연결 정보 조회
     */
    @Transactional(readOnly = true)
    public List<PostSequenceDescription> getPostRecipeDescriptions(UUID postId) {
        return postSequenceDescriptionRepository.findByPostIdOrderByStepSequence(postId);
    }
    
    /**
     * 게시글에 연결된 레시피 조회
     */
    @Transactional(readOnly = true)
    public Recipe getConnectedRecipe(UUID postId) {
        List<PostSequenceDescription> descriptions = getPostRecipeDescriptions(postId);
        if (descriptions.isEmpty()) {
            return null;
        }
        return descriptions.get(0).getRecipeStep().getRecipe();
    }
    
    /**
     * 게시글의 레시피 연결 해제
     */
    @Transactional
    public void unlinkRecipeFromPost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);
        
        postSequenceDescriptionRepository.deleteByPostId(postId);
        
        log.info("레시피 연결 해제 완료 - postId: {}", postId);
    }

    /**
     * 유저 역할별 레시피 공유 게시글 조회 (Enum 검증 자동)
     */
    public Page<PostResponseDto> getRecipePostsByUserRole(Role role, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserRoleAndIsOpenTrueAndDeletedAtIsNull(role, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }
}