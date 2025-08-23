package lazyteam.cooking_hansu.domain.recipe.service;

import lazyteam.cooking_hansu.domain.recipe.dto.*;
import lazyteam.cooking_hansu.domain.recipe.entity.*;
import lazyteam.cooking_hansu.domain.recipe.repository.*;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final IngredientsRepository ingredientsRepository;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

//    레시피 작성 (썸네일 포함)
    public UUID createRecipe(RecipeCreateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();

        // 썸네일 업로드 처리
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "recipes/thumbnails/");
                log.info("레시피 썸네일 업로드 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("레시피 썸네일 업로드 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            // JSON으로 전달된 URL 사용
            thumbnailUrl = requestDto.getThumbnailUrl();
        }

        // 레시피 엔티티 생성 (servings 포함)
        Recipe recipe = Recipe.builder()
                .user(currentUser)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .level(requestDto.getLevel())
                .category(requestDto.getCategory())
                .cookTime(requestDto.getCookTime())
                .servings(requestDto.getServings())  // ← 새로 추가: 인분 수
                .build();

        Recipe savedRecipe = recipeRepository.save(recipe);

        // 재료 저장
        saveIngredients(savedRecipe, requestDto.getIngredients());

        // 조리순서 저장
        saveRecipeSteps(savedRecipe, requestDto.getSteps());

        log.info("레시피 작성 완료. 사용자: {}, 레시피 ID: {}, 인분: {}", 
                currentUser.getEmail(), savedRecipe.getId(), 
                savedRecipe.getServings() != null ? savedRecipe.getServings() + "인분" : "미표시");
        return savedRecipe.getId();
    }

//    상세 레시피 조회
    @Transactional(readOnly = true)
    public RecipeResponseDto getRecipe(UUID recipeId) {
        User currentUser = getCurrentUser();
        Recipe recipe = getRecipeByIdAndUser(recipeId, currentUser);

        // 연관 데이터 조회
        List<Ingredients> ingredients = ingredientsRepository.findByRecipe(recipe);
        List<RecipeStep> steps = recipeStepRepository.findByRecipeOrderByStepSequence(recipe);

        return RecipeResponseDto.fromEntity(recipe, ingredients, steps);
    }

    /**
     * REQ016: 내 레시피 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<RecipeResponseDto> getMyRecipes(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Recipe> recipes = recipeRepository.findByUser(currentUser, pageable);

        return recipes.map(recipe -> {
            List<Ingredients> ingredients = ingredientsRepository.findByRecipe(recipe);
            List<RecipeStep> steps = recipeStepRepository.findByRecipeOrderByStepSequence(recipe);
            return RecipeResponseDto.fromEntity(recipe, ingredients, steps);
        });
    }


    /**
     * REQ017: 레시피 수정 (썸네일 포함)
     */
    public void updateRecipe(UUID recipeId, RecipeUpdateRequestDto requestDto, MultipartFile thumbnail) {
        User currentUser = getCurrentUser();
        Recipe recipe = getRecipeByIdAndUser(recipeId, currentUser);

        // 썸네일 업로드 처리
        String thumbnailUrl = recipe.getThumbnailUrl(); // 기존 URL 유지
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                thumbnailUrl = s3Uploader.upload(thumbnail, "recipes/thumbnails/");
                log.info("레시피 썸네일 업데이트 성공: {}", thumbnailUrl);
            } catch (Exception e) {
                log.error("레시피 썸네일 업데이트 실패: {}", e.getMessage());
                throw new RuntimeException("썸네일 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (requestDto.getThumbnailUrl() != null) {
            // JSON으로 전달된 URL 사용
            thumbnailUrl = requestDto.getThumbnailUrl();
        }

        // 레시피 기본 정보 수정 (servings 포함)
        recipe.updateRecipe(
                requestDto.getTitle(),
                requestDto.getDescription(),
                thumbnailUrl,
                requestDto.getLevel(),
                requestDto.getCategory(),
                requestDto.getCookTime(),
                requestDto.getServings()  // ← 새로 추가: 인분 수
        );

        // 재료 정보 갱신
        if (requestDto.getIngredients() != null) {
            ingredientsRepository.deleteByRecipe(recipe);
            saveIngredientsForUpdate(recipe, requestDto.getIngredients());
        }

        // 조리순서 정보 갱신
        if (requestDto.getSteps() != null) {
            recipeStepRepository.deleteByRecipe(recipe);
            saveRecipeStepsForUpdate(recipe, requestDto.getSteps());
        }

        log.info("레시피 수정 완료. 사용자: {}, 레시피 ID: {}, 인분: {}", 
                currentUser.getEmail(), recipeId, 
                recipe.getServings() != null ? recipe.getServings() + "인분" : "미표시");
    }


    /**
     * REQ018: 레시피 삭제
     */
    public void deleteRecipe(UUID recipeId) {
        User currentUser = getCurrentUser();
        Recipe recipe = getRecipeByIdAndUser(recipeId, currentUser);

        // 연관 데이터 삭제 (Cascade 설정이 없으므로 수동 삭제)
        ingredientsRepository.deleteByRecipe(recipe);
        recipeStepRepository.deleteByRecipe(recipe);
        recipeRepository.delete(recipe);

        log.info("레시피 삭제 완료. 사용자: {}, 레시피 ID: {}", currentUser.getEmail(), recipeId);
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
     * 레시피 조회 (권한 확인 포함)
     */
    private Recipe getRecipeByIdAndUser(UUID recipeId, User user) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 레시피입니다."));

        if (!recipe.isOwnedBy(user)) {
            throw new IllegalArgumentException("레시피에 대한 권한이 없습니다.");
        }

        return recipe;
    }

    /**
     * 재료 정보 저장
     */
    private void saveIngredients(Recipe recipe, List<RecipeCreateRequestDto.IngredientRequestDto> ingredientDtos) {
        List<Ingredients> ingredients = new ArrayList<>();

        for (RecipeCreateRequestDto.IngredientRequestDto dto : ingredientDtos) {
            Ingredients ingredient = Ingredients.builder()
                    .recipe(recipe)
                    .name(dto.getName())
                    .amount(dto.getAmount())
                    .build();
            ingredients.add(ingredient);
        }

        ingredientsRepository.saveAll(ingredients);
    }

    /**
     * 조리순서 정보 저장
     */
    private void saveRecipeSteps(Recipe recipe, List<RecipeCreateRequestDto.RecipeStepRequestDto> stepDtos) {
        List<RecipeStep> steps = new ArrayList<>();

        for (RecipeCreateRequestDto.RecipeStepRequestDto dto : stepDtos) {
            RecipeStep step = RecipeStep.builder()
                    .recipe(recipe)
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
    private void saveIngredientsForUpdate(Recipe recipe, List<RecipeUpdateRequestDto.IngredientUpdateDto> ingredientDtos) {
        List<Ingredients> ingredients = new ArrayList<>();

        for (RecipeUpdateRequestDto.IngredientUpdateDto dto : ingredientDtos) {
            Ingredients ingredient = Ingredients.builder()
                    .recipe(recipe)
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
    private void saveRecipeStepsForUpdate(Recipe recipe, List<RecipeUpdateRequestDto.RecipeStepUpdateDto> stepDtos) {
        List<RecipeStep> steps = new ArrayList<>();

        for (RecipeUpdateRequestDto.RecipeStepUpdateDto dto : stepDtos) {
            RecipeStep step = RecipeStep.builder()
                    .recipe(recipe)
                    .stepSequence(dto.getStepSequence())
                    .content(dto.getContent())
                    .build();
            steps.add(step);
        }

        recipeStepRepository.saveAll(steps);
    }
}