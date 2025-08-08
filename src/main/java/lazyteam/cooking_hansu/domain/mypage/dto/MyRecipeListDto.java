package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MyRecipeListDto {
    private UUID id;
    private String title;
    private List<String> ingredients;
    private LocalDateTime createdAt;
    private String thumbnailUrl;
}
