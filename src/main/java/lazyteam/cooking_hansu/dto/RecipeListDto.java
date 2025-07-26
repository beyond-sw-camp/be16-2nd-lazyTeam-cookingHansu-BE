package lazyteam.cooking_hansu.dto;

import lazyteam.cooking_hansu.domain.Recipe;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RecipeListDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String thumbnailUrl;
    // 일단 좋아요 수, 북마크 수는 제외하고 함

    public static RecipeListDto fromEntity(Recipe recipe) {
        return RecipeListDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .category(recipe.getCategory().getLabel())
                .thumbnailUrl(recipe.getThumbnailUrl())
                .build();
    }
}
