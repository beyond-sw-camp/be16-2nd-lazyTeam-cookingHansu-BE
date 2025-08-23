package lazyteam.cooking_hansu.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDto {

    @NotBlank(message = "게시글 작성을 필수입니다.")
    @Size(max = 255, message = "게시글 제목은 255자 이하여야 합니다.")
    private String title;

    @Size(max = 2000, message = "게시글 설명은 2000자 이하여야 합니다.")
    private String description;

    @NotNull(message = "카테고리는 필수입니다.")
    private CategoryEnum category;

    @Size(max = 512, message = "썸네일 url은 512자 이하여야 합니다.")
    private String thumbnailUrl;

    @Builder.Default
    @NotNull(message = "공개 여부는 필수입니다.")
    private Boolean isOpen = true;

    // 유효성 검증
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                category != null;
    }
}
