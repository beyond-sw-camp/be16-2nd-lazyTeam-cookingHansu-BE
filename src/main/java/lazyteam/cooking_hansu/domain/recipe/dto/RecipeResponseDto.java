package lazyteam.cooking_hansu.domain.recipe.dto;

import lazyteam.cooking_hansu.domain.recipe.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 레시피 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDto {

    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private LevelType level;
    private CategoryType category;
    private Integer cookTime;
    // ========== 인분 정보: 숫자만 저장 ==========
    private Integer servings;        // 몇 인분 (숫자만)
    private String userNickname;
    private LocalDateTime createdAt;      // 생성일시 추가
    private LocalDateTime updatedAt;      // 수정일시 추가
    private List<IngredientResponseDto> ingredients;
    private List<RecipeStepResponseDto> steps;

    /**
     * Entity -> DTO 변환
     */
    public static RecipeResponseDto fromEntity(Recipe recipe, List<Ingredients> ingredients, List<RecipeStep> steps) {
        return RecipeResponseDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .level(recipe.getLevel())
                .category(recipe.getCategory())
                .cookTime(recipe.getCookTime())
                .servings(recipe.getServings())                    // ← 숫자만 저장
                .userNickname(recipe.getUser().getNickname())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(ingredients.stream()
                        .map(IngredientResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .steps(steps.stream()
                        .map(RecipeStepResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 인분을 텍스트로 변환하는 헬퍼 메서드 (프론트엔드에서 사용)
     * null인 경우 null 반환
     */
    public String getServingsText() {
        return this.servings != null ? this.servings + "인분" : null;
    }

    /**
     * 재료 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientResponseDto {
        private UUID id;
        private String name;
        private String amount;

        public static IngredientResponseDto fromEntity(Ingredients ingredient) {
            return IngredientResponseDto.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .amount(ingredient.getAmount())
                    .build();
        }
    }

    /**
     * 조리순서 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeStepResponseDto {
        private UUID id;
        private Integer stepSequence;
        private String content;

        public static RecipeStepResponseDto fromEntity(RecipeStep step) {
            return RecipeStepResponseDto.builder()
                    .id(step.getId())
                    .stepSequence(step.getStepSequence())
                    .content(step.getContent())
                    .build();
        }
    }
}
