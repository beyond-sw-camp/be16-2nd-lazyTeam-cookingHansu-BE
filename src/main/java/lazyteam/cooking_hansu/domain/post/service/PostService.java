package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.common.enums.FilterSort;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.post.dto.*;
import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Ingredients;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.entity.RecipeStep;
import lazyteam.cooking_hansu.domain.post.repository.IngredientsRepository;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.post.repository.RecipeStepRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final IngredientsRepository ingredientsRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final S3Uploader s3Uploader;
    private final InteractionService interactionService;  // 추가


    private User getCurrentUser() {
        UUID userId = AuthUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Post getPostByIdAndUser(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.isOwnedBy(user)) {
            throw new IllegalArgumentException("게시글에 대한 권한이 없습니다.");
        }
        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }
        return post;
    }


    public UUID createPost(PostCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();

        // 썸네일 업로드 처리
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                log.info("S3 업로드 시작 - 파일명: {}, 크기: {} bytes", thumbnail.getOriginalFilename(), thumbnail.getSize());
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("Post 썸네일 업로드 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("Post 썸네일 업로드 실패: {}", e.getMessage(), e);
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            thumbnailUrl = requestDto.getThumbnailUrl();
            log.info("기존 썸네일 URL 사용: {}", thumbnailUrl);
        } else {
            log.warn("썸네일이 제공되지 않음 - thumbnail: {}, requestDto.thumbnailUrl: {}",
                    thumbnail, requestDto.getThumbnailUrl());
        }

        // Post 엔티티 생성
        Post post = Post.builder()
                .user(currentUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .level(requestDto.getLevel())
                .cookTime(requestDto.getCookTime())
                .serving(requestDto.getServing())
                .cookTip(requestDto.getCookTip())
                .thumbnailUrl(thumbnailUrl)
                .isOpen(requestDto.getIsOpen())
                .build();

        Post savedPost = postRepository.save(post);

        // 재료 저장
        if (requestDto.getIngredients() != null && !requestDto.getIngredients().isEmpty()) {
            saveIngredients(savedPost, requestDto.getIngredients());
        }

        // 조리순서 저장
        if (requestDto.getSteps() != null && !requestDto.getSteps().isEmpty()) {
            saveRecipeSteps(savedPost, requestDto.getSteps());
        }

        log.info("통합 Post 작성 완료. 사용자: {}, Post ID: {}, 인분: {}인분",
                currentUser.getEmail(), savedPost.getId(), savedPost.getServing());
        return savedPost.getId();
    }

    /**
     * Post 상세 조회 (재료, 조리순서 포함) - 비회원도 접근 가능
     */
    @Transactional(readOnly = true)
    public PostResponseDto getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        // 삭제되었거나 비공개 게시글인 경우 접근 차단
        if (post.isDeleted() || !post.getIsOpen()) {
            throw new EntityNotFoundException("접근할 수 없는 게시글입니다.");
        }

        // 연관 데이터 조회
        List<Ingredients> ingredients = ingredientsRepository.findByPost(post);
        List<RecipeStep> steps = recipeStepRepository.findByPostOrderByStepSequence(post);

        Boolean isLiked = null;
        Boolean isBookmarked = null;

        try {
            UUID currentUserId = AuthUtils.getCurrentUserId();
            if (currentUserId != null) {
                isLiked = interactionService.isPostLikedByCurrentUser(postId);
                isBookmarked = interactionService.isPostBookmarkedByCurrentUser(postId);
            }
        } catch (Exception e) {
        }

        return PostResponseDto.fromEntity(post, ingredients, steps, isLiked, isBookmarked);
    }


    public void updatePost(UUID postId, PostUpdateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // 기존 썸네일 URL 저장
        String oldThumbnailUrl = post.getThumbnailUrl();
        String thumbnailUrl = oldThumbnailUrl; // 기본값은 기존 URL

        // 새로운 썸네일이 업로드된 경우
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                // 새 이미지 업로드
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("Post 썸네일 업데이트 성공: {}", thumbnailUrl);

                // 기존 이미지 삭제 (기존 URL이 있는 경우에만)
                if (oldThumbnailUrl != null && !oldThumbnailUrl.isEmpty()) {
                    try {
                        s3Uploader.delete(oldThumbnailUrl);
                        log.info("기존 Post 썸네일 삭제 성공: {}", oldThumbnailUrl);
                    } catch (Exception deleteException) {
                        log.warn("기존 Post 썸네일 삭제 실패 (계속 진행): {}", deleteException.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Post 썸네일 업데이트 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            thumbnailUrl = requestDto.getThumbnailUrl();
        }

        // 나머지 기존 코드 그대로...
        PostUpdateData updateData = PostUpdateData.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .category(requestDto.getCategory())
                .level(requestDto.getLevel())
                .cookTime(requestDto.getCookTime())
                .serving(requestDto.getServing())
                .cookTip(requestDto.getCookTip())
                .isOpen(requestDto.getIsOpen())
                .build();

        post.updatePost(updateData);
        // 재료 정보 갱신
        if (requestDto.getIngredients() != null) {
            ingredientsRepository.deleteByPost(post);
            saveIngredients(post, requestDto.getIngredients());
        }

        // 조리순서 정보 갱신
        if (requestDto.getSteps() != null) {
            recipeStepRepository.deleteByPost(post);
            saveRecipeSteps(post, requestDto.getSteps());
        }

        log.info("Post 수정 완료. 사용자: {}, Post ID: {}", currentUser.getEmail(), postId);
    }

    /**
     * Post 삭제
     */
    @Transactional
    public void deletePost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // 연관 데이터 삭제 (Cascade 설정으로 자동 삭제되지만 명시적으로)
        ingredientsRepository.deleteByPost(post);
        recipeStepRepository.deleteByPost(post);

        // Soft Delete
        post.softDelete();
        postRepository.save(post);

        log.info("Post 삭제 완료. 사용자: {}, Post ID: {}", currentUser.getEmail(), postId);
    }

    /**
     * Post 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPostList(Role role, CategoryEnum category,
                                                 FilterSort filterSort, Pageable pageable) {

        Page<Post> postsPage = postRepository.findPostsByFilters(role, category, pageable);

        UUID currentUserId = null;
        try {
            currentUserId = AuthUtils.getCurrentUserId();
        } catch (Exception e) {
            log.debug("비회원 접근: {}", e.getMessage());
        }

        final UUID finalCurrentUserId = currentUserId;

        Page<PostListResponseDto> result = postsPage.map(post -> {
            Boolean isLiked = null;
            Boolean isBookmarked = null;

            if (finalCurrentUserId != null) {
                try {
                    isLiked = interactionService.isPostLikedByCurrentUser(post.getId());
                    isBookmarked = interactionService.isPostBookmarkedByCurrentUser(post.getId());
                } catch (Exception e) {
                    log.debug("상태 확인 실패 - postId: {}", post.getId());
                }
            }

            return PostListResponseDto.fromEntity(post, isLiked, isBookmarked);
        });

        log.info("Post 목록 조회 - role: {}, category: {}, filterSort: {}, page: {}, totalElements: {}",
                role, category, filterSort, pageable.getPageNumber(), postsPage.getTotalElements());

        return result;
    }

    // ========== 유틸리티 메서드 ==========

    private void saveIngredients(Post post, List<?> ingredientDtos) {
        if (ingredientDtos == null || ingredientDtos.isEmpty()) return;

        List<Ingredients> ingredients = ingredientDtos.stream()
                .map(dto -> {
                    String name, amount;

                    // 타입 확인해서 필드 추출
                    if (dto instanceof PostCreateRequestDto.IngredientRequestDto createDto) {
                        name = createDto.getName();
                        amount = createDto.getAmount();
                    } else if (dto instanceof PostUpdateRequestDto.IngredientUpdateDto updateDto) {
                        name = updateDto.getName();
                        amount = updateDto.getAmount();
                    } else {
                        throw new IllegalArgumentException("지원하지않는 DTO 타입이 오류입니다.");
                    }

                    return Ingredients.builder()
                            .post(post)
                            .name(name)
                            .amount(amount)
                            .build();
                })
                .toList();

        ingredientsRepository.saveAll(ingredients);
    }

    private void saveRecipeSteps(Post post, List<?> stepDtos) {
        if (stepDtos == null || stepDtos.isEmpty()) return;

        List<RecipeStep> steps = stepDtos.stream()
                .map(dto -> {
                    Integer stepSequence;
                    String content;
                    String description;

                    // 타입 확인해서 필드 추출
                    if (dto instanceof PostCreateRequestDto.RecipeStepRequestDto createDto) {
                        stepSequence = createDto.getStepSequence();
                        content = createDto.getContent();
                        description = createDto.getDescription();
                    } else if (dto instanceof PostUpdateRequestDto.RecipeStepUpdateDto updateDto) {
                        stepSequence = updateDto.getStepSequence();
                        content = updateDto.getContent();
                        description = updateDto.getDescription();
                    } else {
                        throw new IllegalArgumentException("지원하지않는 DTO 타입이 오류입니다.");
                    }

                    return RecipeStep.builder()
                            .post(post)
                            .stepSequence(stepSequence)
                            .content(content)
                            .description(description)
                            .build();
                })
                .toList();

        recipeStepRepository.saveAll(steps);
    }
}