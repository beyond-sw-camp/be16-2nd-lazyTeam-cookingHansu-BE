package lazyteam.cooking_hansu.domain.post.dto;

import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lombok.*;

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

    // 연결할 레시피 ID 변경 (선택적)
    private UUID recipeId;

//    연결해제여부확인
    public boolean shouldRemoveRecipe() {
        return recipeId != null && recipeId.toString().equals("remove");
    }

}
