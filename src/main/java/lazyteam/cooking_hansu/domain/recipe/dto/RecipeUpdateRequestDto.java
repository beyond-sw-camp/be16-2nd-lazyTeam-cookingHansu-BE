package lazyteam.cooking_hansu.domain.recipe.dto;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * 레시피 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeUpdateRequestDto {

    @Size(max = 255, message = "레시피 제목은 255자 이하여야 합니다")
    private String title;

    @Size(max = 1000, message = "레시피 설명은 1000자 이하여야 합니다")
    private String description;

    @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다")
    private String thumbnailUrl;

    private LevelEnum level;

    private CategoryEnum category;

    @Min(value = 1, message = "조리 시간은 1분 이상이어야 합니다")
    private Integer cookTime;

    // ========== 새로 추가: 인분 수 필드 ==========
    /**
     * 몇 인분인지 나타내는 필드 (1인분, 2인분, 4인분 등)
     * 선택사항 - 양념, 소스류 등은 인분을 명시하지 않을 수 있음
     */
    @Min(value = 1, message = "인분 수는 1 이상이어야 합니다")
    @Max(value = 20, message = "인분 수는 20 이하여야 합니다")
    private Integer servings;

    @Valid
    private List<IngredientUpdateDto> ingredients;

    @Valid
    private List<RecipeStepUpdateDto> steps;

    /**
     * 재료 수정 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientUpdateDto {
        
        @NotBlank(message = "재료명은 필수입니다")
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다")
        private String name;

        @NotBlank(message = "재료량은 필수입니다")
        @Size(max = 255, message = "재료량은 255자 이하여야 합니다")
        private String amount;
    }

    /**
     * 조리순서 수정 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStepUpdateDto {
        
        @NotNull(message = "순서 번호는 필수입니다")
        @Min(value = 1, message = "순서 번호는 1 이상이어야 합니다")
        private Integer stepSequence;

        @NotBlank(message = "조리 단계 설명은 필수입니다")
        @Size(max = 255, message = "조리 단계 설명은 255자 이하여야 합니다")
        private String content;
    }
}
