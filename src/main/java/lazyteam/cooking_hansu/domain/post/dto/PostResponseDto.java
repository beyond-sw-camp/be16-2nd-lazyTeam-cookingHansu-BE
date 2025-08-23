package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.post.entity.Post;
import lazyteam.cooking_hansu.domain.recipe.entity.Ingredients;
import lazyteam.cooking_hansu.domain.recipe.entity.Recipe;
import lazyteam.cooking_hansu.domain.recipe.entity.RecipeStep;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private UUID id;
    private String title;
    private String description;
    private CategoryEnum category;
    private String thumbnailUrl;
    private Long likeCount;
    private Long viewCount;
    private Long bookmarkCount;
    private Boolean isOpen;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserInfoDto user;
    private RecipeInfoDto recipe;
    private List<StepDescriptionDto> stepDescriptions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeInfoDto {
        private UUID id;
        private String title;
        private String description;
        private LevelEnum level;
        private CategoryEnum category;
        private int cookTime;
        private Integer servings;
        private List<IngredientDto> ingredients;
        private List<RecipeStepDto> steps;

        public static RecipeInfoDto fromEntity(Recipe recipe) {
            if (recipe == null) return null;
            return RecipeInfoDto.builder()
                    .id(recipe.getId())
                    .title(recipe.getTitle())
                    .description(recipe.getDescription())
                    .level(recipe.getLevel())
                    .category(recipe.getCategory())
                    .cookTime(recipe.getCookTime())
                    .servings(recipe.getServings())
                    .ingredients(recipe.getIngredients() != null
                            ? recipe.getIngredients().stream().map(IngredientDto::fromEntity).toList()
                            : List.of())
                    .steps(recipe.getSteps() != null
                            ? recipe.getSteps().stream().map(RecipeStepDto::fromEntity).toList()
                            : List.of())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientDto {
        private UUID id;
        private String name;
        private String amount;

        public static IngredientDto fromEntity(Ingredients ingredient) {
            if (ingredient == null) return null;
            return IngredientDto.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .amount(ingredient.getAmount())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeStepDto {
        private UUID id;
        private Integer stepSequence;
        private String content;

        public static RecipeStepDto fromEntity(RecipeStep step) {
            if (step == null) return null;
            return RecipeStepDto.builder()
                    .id(step.getId())
                    .stepSequence(step.getStepSequence())
                    .content(step.getContent())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StepDescriptionDto {
        private UUID stepId;
        private Integer stepSequence;
        private String originalContent;
        private String additionalContent;

        public static StepDescriptionDto fromEntity(lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription description) {
            if (description == null) return null;
            return StepDescriptionDto.builder()
                    .stepId(description.getRecipeStep().getId())
                    .stepSequence(description.getRecipeStep().getStepSequence())
                    .originalContent(description.getRecipeStep().getContent())
                    .additionalContent(description.getContent())
                    .build();
        }
    }

    public static PostResponseDto fromEntity(Post post, Recipe recipe, List<lazyteam.cooking_hansu.domain.recipe.entity.PostSequenceDescription> descriptions) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .category(post.getCategory())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .bookmarkCount(post.getBookmarkCount())
                .isOpen(post.getIsOpen())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .user(UserInfoDto.fromEntity(post.getUser()))
                .recipe(RecipeInfoDto.fromEntity(recipe))
                .stepDescriptions(descriptions != null
                        ? descriptions.stream().map(StepDescriptionDto::fromEntity).toList()
                        : List.of())
                .build();
    }

    public static PostResponseDto fromEntity(Post post) {
        return fromEntity(post, null, null);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private UUID id;
        private String nickname;
        private String profileImageUrl;
        private String role;

        public static UserInfoDto fromEntity(lazyteam.cooking_hansu.domain.user.entity.common.User user) {
            if (user == null) return null;
            return UserInfoDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .role(user.getRole() != null ? user.getRole().name() : "GENERAL")
                    .build();
        }

        public String getRoleKorean() {
            if (role == null) return "일반 사용자";
            return switch (role) {
                case "GENERAL" -> "일반 사용자";
                case "CHEF" -> "셰프";
                case "OWNER" -> "자영업자";
                default -> "일반 사용자";
            };
        }
    }
}