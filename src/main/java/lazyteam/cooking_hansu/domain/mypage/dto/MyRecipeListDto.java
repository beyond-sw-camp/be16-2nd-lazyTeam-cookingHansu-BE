package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MyRecipeListDto {
    private String title;
    private List<String> ingredients;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
}
