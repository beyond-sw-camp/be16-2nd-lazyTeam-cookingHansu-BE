package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.dto.PostUpdateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostListResponseDto;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.post.entity.Ingredients;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.post.entity.RecipeStep;
import lazyteam.cooking_hansu.domain.post.repository.IngredientsRepository;
import lazyteam.cooking_hansu.domain.post.repository.PostRepository;
import lazyteam.cooking_hansu.domain.post.repository.RecipeStepRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final IngredientsRepository ingredientsRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    private User getCurrentUser() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        return userRepository.findById(testUserId)
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

    @Transactional
    public UUID createPost(PostCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();

        // 썸네일 업로드 처리
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("Post 썸네일 업로드 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("Post 썸네일 업로드 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            thumbnailUrl = requestDto.getThumbnailUrl();
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
     * Post 상세 조회 (재료, 조리순서 포함)
     */
    @Transactional(readOnly = true)
    public PostResponseDto getPost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        User currentUser = getCurrentUser();
        if (!post.getIsOpen() && !post.isOwnedBy(currentUser)) {
            throw new IllegalArgumentException("비공개 게시글에 대한 권한이 없습니다.");
        }

        // 연관 데이터 조회
        List<Ingredients> ingredients = ingredientsRepository.findByPost(post);
        List<RecipeStep> steps = recipeStepRepository.findByPostOrderByStepSequence(post);

        return PostResponseDto.fromEntity(post, ingredients, steps);
    }

    /**
     * Post 수정
     */
    @Transactional
    public void updatePost(UUID postId, PostUpdateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        // 썸네일 업로드 처리
        String thumbnailUrl = post.getThumbnailUrl(); // 기존 URL 유지
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("Post 썸네일 업데이트 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("Post 썸네일 업데이트 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            thumbnailUrl = requestDto.getThumbnailUrl();
        }

        // Post 기본 정보 수정
        post.updatePost(
                requestDto.getTitle(),
                requestDto.getDescription(),
                thumbnailUrl,
                requestDto.getCategory(),
                requestDto.getLevel(),
                requestDto.getCookTime(),
                requestDto.getServing(),
                requestDto.getCookTip(),
                requestDto.getIsOpen()
        );

        // 재료 정보 갱신
        if (requestDto.getIngredients() != null) {
            ingredientsRepository.deleteByPost(post);
            saveIngredientsForUpdate(post, requestDto.getIngredients());
        }

        // 조리순서 정보 갱신
        if (requestDto.getSteps() != null) {
            recipeStepRepository.deleteByPost(post);
            saveRecipeStepsForUpdate(post, requestDto.getSteps());
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
     * Post 목록 조회 (강의 팀메이트 스타일)
     */
    @Transactional(readOnly = true) 
    public List<PostListResponseDto> getPostList(String userType, CategoryEnum category, String sort, int page, int size) {
        
        // 강의쪽과 같은 정렬 방식
        Sort sorting = switch (sort) {
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "likes" -> Sort.by(Sort.Direction.DESC, "likeCount");  
            case "bookmarks" -> Sort.by(Sort.Direction.DESC, "bookmarkCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // latest
        };
        
        Pageable pageable = PageRequest.of(page, size, sorting);
        
        // 기본 조회 (강의쪽과 동일한 패턴)
        Page<Post> postsPage;
        if (category != null) {
            postsPage = postRepository.findByDeletedAtIsNullAndIsOpenTrueAndCategory(category, pageable);
        } else {
            postsPage = postRepository.findByDeletedAtIsNullAndIsOpenTrue(pageable);
        }
        
        // 사용자 타입 필터링 후 DTO 변환 (강의쪽 방식)
        return postsPage.stream()
                .filter(post -> userType == null || post.getUser().getRole().name().equalsIgnoreCase(userType))
                .map(PostListResponseDto::fromEntity)
                .toList();
    }

    // ========== 유틸리티 메서드 ==========

    /**
     * 재료 정보 저장 (생성용)
     */
    private void saveIngredients(Post post, List<PostCreateRequestDto.IngredientRequestDto> ingredientDtos) {
        List<Ingredients> ingredients = new ArrayList<>();

        for (PostCreateRequestDto.IngredientRequestDto dto : ingredientDtos) {
            Ingredients ingredient = Ingredients.builder()
                    .post(post)
                    .name(dto.getName())
                    .amount(dto.getAmount())
                    .build();
            ingredients.add(ingredient);
        }

        ingredientsRepository.saveAll(ingredients);
    }

    /**
     * 조리순서 정보 저장 (생성용)
     */
    private void saveRecipeSteps(Post post, List<PostCreateRequestDto.RecipeStepRequestDto> stepDtos) {
        List<RecipeStep> steps = new ArrayList<>();

        for (PostCreateRequestDto.RecipeStepRequestDto dto : stepDtos) {
            RecipeStep step = RecipeStep.builder()
                    .post(post)
                    .stepSequence(dto.getStepSequence())
                    .content(dto.getContent())
                    .build();
            steps.add(step);
        }

        recipeStepRepository.saveAll(steps);
    }

    /**
     * 재료 정보 저장 (수정용)
     */
    private void saveIngredientsForUpdate(Post post, List<PostUpdateRequestDto.IngredientUpdateDto> ingredientDtos) {
        List<Ingredients> ingredients = new ArrayList<>();

        for (PostUpdateRequestDto.IngredientUpdateDto dto : ingredientDtos) {
            Ingredients ingredient = Ingredients.builder()
                    .post(post)
                    .name(dto.getName())
                    .amount(dto.getAmount())
                    .build();
            ingredients.add(ingredient);
        }

        ingredientsRepository.saveAll(ingredients);
    }

    /**
     * 조리순서 정보 저장 (수정용)
     */
    private void saveRecipeStepsForUpdate(Post post, List<PostUpdateRequestDto.RecipeStepUpdateDto> stepDtos) {
        List<RecipeStep> steps = new ArrayList<>();

        for (PostUpdateRequestDto.RecipeStepUpdateDto dto : stepDtos) {
            RecipeStep step = RecipeStep.builder()
                    .post(post)
                    .stepSequence(dto.getStepSequence())
                    .content(dto.getContent())
                    .build();
            steps.add(step);
        }

        recipeStepRepository.saveAll(steps);
    }
}
