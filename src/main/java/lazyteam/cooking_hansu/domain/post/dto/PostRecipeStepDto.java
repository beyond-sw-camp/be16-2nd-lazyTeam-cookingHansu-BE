package lazyteam.cooking_hansu.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 게시글의 레시피 단계별 추가 설명 DTO
 */
@Getter
@Setter
public class PostRecipeStepDto {
    private UUID stepId;        // RecipeStep의 ID
    private String content;     // 해당 단계에 대한 추가 설명/코멘트
}
