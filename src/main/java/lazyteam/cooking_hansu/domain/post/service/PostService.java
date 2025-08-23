package lazyteam.cooking_hansu.domain.post.service;

import lazyteam.cooking_hansu.domain.post.dto.PostCreateRequestDto;
import lazyteam.cooking_hansu.domain.post.dto.PostResponseDto;
import lazyteam.cooking_hansu.domain.post.dto.PostUpdateRequestDto;
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
    public UUID createRecipePost(PostCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();

        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
        }

        Post post = Post.builder()
                .user(currentUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .category(requestDto.getCategory())
                .thumbnailUrl(thumbnailUrl)
                .isOpen(requestDto.getIsOpen())
                .build();

        postRepository.save(post);

        if (requestDto.hasRecipe()) {
            linkRecipeToPost(post.getId(), requestDto.getRecipe(), requestDto.getStepDescriptions());
        }

        return post.getId();
    }
    @Transactional
    public void linkRecipeToPost(UUID postId, UUID recipeId, List<PostRecipeStepDto> stepDescriptions) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("레시피를 찾을 수 없습니다."));

        if (stepDescriptions != null && !stepDescriptions.isEmpty()) {
            for (PostRecipeStepDto stepDto : stepDescriptions) {
                RecipeStep recipeStep = recipeStepRepository.findById(stepDto.getStepId())
                        .orElseThrow(() -> new EntityNotFoundException("조리순서를 찾을 수 없습니다."));
                if (!recipeStep.getRecipe().getId().equals(recipeId)) {
                    throw new IllegalArgumentException("잘못된 조리순서입니다.");
                }
                PostSequenceDescription description = PostSequenceDescription.builder()
                        .post(post)
                        .recipeStep(recipeStep)
                        .content(stepDto.getContent())
                        .build();
                postSequenceDescriptionRepository.save(description);
            }
        }
    }

    @Transactional(readOnly = true)
    public PostResponseDto getRecipePost(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다."));

        if (post.isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다.");
        }

        Recipe connectedRecipe = getConnectedRecipe(postId);
        List<PostSequenceDescription> descriptions = getPostRecipeDescriptions(postId);

        User currentUser = getCurrentUser();
        if (!post.getIsOpen() && !post.isOwnedBy(currentUser)) {
            throw new IllegalArgumentException("비공개 게시글에 대한 권한이 없습니다.");
        }

        return PostResponseDto.fromEntity(post, connectedRecipe, descriptions);
    }

    @Transactional(readOnly = true)
    public List<PostSequenceDescription> getPostRecipeDescriptions(UUID postId) {
        return postSequenceDescriptionRepository.findByPostIdOrderByStepSequence(postId);
    }

    @Transactional(readOnly = true)
    public Recipe getConnectedRecipe(UUID postId) {
        List<PostSequenceDescription> descriptions = getPostRecipeDescriptions(postId);
        if (descriptions.isEmpty()) {
            return null;
        }
        return recipeRepository.findById(descriptions.get(0).getRecipeStep().getRecipe().getId())
                .orElse(null);
    }

    @Transactional
    public void unlinkRecipeFromPost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        postSequenceDescriptionRepository.deleteByPostId(postId);

        log.info("레시피 연결 해제 완료 - postId: {}", postId);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getRecipePostsByUserRole(Role role, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserRoleAndIsOpenTrueAndDeletedAtIsNull(role, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getRecipePostsByCategory(CategoryEnum category, Pageable pageable) {
        Page<Post> posts = postRepository.findByCategoryAndIsOpenTrueAndDeletedAtIsNull(category, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getRecipePostsByCategoryAndUserRole(CategoryEnum category, Role role, Pageable pageable) {
        Page<Post> posts = postRepository.findByCategoryAndUserRoleAndIsOpenTrueAndDeletedAtIsNull(category, role, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getMyRecipePosts(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Post> posts = postRepository.findByUserAndDeletedAtIsNull(currentUser, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getRecipePostsByUser(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Page<Post> posts = postRepository.findByUserAndIsOpenTrueAndDeletedAtIsNull(user, pageable);
        return posts.map(PostResponseDto::fromEntity);
    }

    @Transactional
    public void updateRecipePost(UUID postId, PostUpdateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);

        String thumbnailUrl = post.getThumbnailUrl();
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "posts/thumbnails/");
                log.info("게시글 썸네일 업데이트 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("게시글 썸네일 업데이트 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드 중 오류 발생", e);
            }
        }

        post.updatePost(requestDto.getTitle(), requestDto.getDescription(), thumbnailUrl, requestDto.getCategory(), requestDto.getIsOpen());
        postRepository.save(post);

        if (requestDto.shouldRemoveRecipe()) {
            unlinkRecipeFromPost(postId);
        } else if (requestDto.hasRecipe()) {
            unlinkRecipeFromPost(postId);
            linkRecipeToPost(postId, requestDto.getRecipe(), requestDto.getStepDescriptions());
        }

        log.info("게시글 업데이트 완료 - postId: {}", postId);
    }

    @Transactional
    public void deleteRecipePost(UUID postId) {
        User currentUser = getCurrentUser();
        Post post = getPostByIdAndUser(postId, currentUser);
        post.softDelete();
        postRepository.save(post);
        log.info("게시글 소프트 삭제 완료 - postId: {}", postId);


    }


}