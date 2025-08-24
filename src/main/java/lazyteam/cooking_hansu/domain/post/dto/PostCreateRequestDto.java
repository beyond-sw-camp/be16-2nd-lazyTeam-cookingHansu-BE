package lazyteam.cooking_hansu.domain.post.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {

    @NotBlank(message = "게시글 제목은 필수입니다.")
    @Size(max = 20, message = "게시글 제목은 20자 이하여야 합니다.")
    private String title;

    @Size(max = 2000, message = "게시글 설명은 2000자 이하여야 합니다.")
    private String description;

    @NotNull(message = "카테고리는 필수입니다.")
    private CategoryEnum category;

    @NotNull(message = "난이도는 필수입니다.")
    private LevelEnum level;

    @NotNull(message = "조리 시간은 필수입니다.")
    @Min(value = 1, message = "조리 시간은 1분 이상이어야 합니다.")
    @Max(value = 999, message = "조리 시간은 999분 이하여야 합니다.")
    private Integer cookTime;

    @NotNull(message = "인분 수는 필수입니다.")
    @Min(value = 1, message = "인분 수는 1 이상이어야 합니다.")
    @Max(value = 20, message = "인분 수는 20 이하여야 합니다.")
    private Integer serving;

    @Size(max = 2000, message = "요리 팁은 2000자 이하여야 합니다.")
    private String cookTip;

    @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다.")
    private String thumbnailUrl;

    @Builder.Default
    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isOpen = true;

    @Valid
    private List<IngredientRequestDto> ingredients;

    @Valid
    private List<RecipeStepRequestDto> steps;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IngredientRequestDto {
        @NotBlank(message = "재료명은 필수입니다.")
        @Size(max = 255, message = "재료명은 255자 이하여야 합니다.")
        private String name;

        @NotBlank(message = "재료 양은 필수입니다.")
        @Size(max = 255, message = "재료 양은 255자 이하여야 합니다.")
        private String amount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeStepRequestDto {
        @NotNull(message = "단계 번호는 필수입니다.")
        @Min(value = 1, message = "단계 번호는 1 이상이어야 합니다.")
        private Integer stepSequence;

        @NotBlank(message = "단계 설명은 필수입니다.")
        @Size(max = 255, message = "단계 설명은 255자 이하여야 합니다.")
        private String content;

    }
}