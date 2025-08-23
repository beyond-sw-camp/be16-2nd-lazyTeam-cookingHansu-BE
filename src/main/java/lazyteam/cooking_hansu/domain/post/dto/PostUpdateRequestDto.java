package lazyteam.cooking_hansu.domain.post.dto;

import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {

    @Size(max = 255, message = "게시글 제목은 255자 이하여야 합니다")
    private String title;

    @Size(max = 2000, message = "게시글 설명은 2000자 이하여야 합니다")
    private String description;

    private CategoryEnum category;

    @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다")
    private String thumbnailUrl;

    private Boolean isOpen;

    private UUID recipe;

    private List<PostRecipeStepDto> stepDescriptions;

    public boolean hasRecipe() {
        return recipe != null;
    }

    public boolean hasStepDescriptions() {
        return stepDescriptions != null && !stepDescriptions.isEmpty();
    }

    public boolean shouldRemoveRecipe() {
        return recipe != null && recipe.toString().equals("remove");
    }

}