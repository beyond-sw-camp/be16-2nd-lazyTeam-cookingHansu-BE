package lazyteam.cooking_hansu.domain.post.dto;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateData {
    private String title;
    private String description;
    private String thumbnailUrl;
    private CategoryEnum category;
    private LevelEnum level;
    private Integer cookTime;
    private Integer serving;
    private String cookTip;
    private Boolean isOpen;
}
