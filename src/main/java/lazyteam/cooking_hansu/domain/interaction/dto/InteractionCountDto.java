package lazyteam.cooking_hansu.domain.interaction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InteractionCountDto {
    private final int likeCount;
    private final int bookmarkCount;
    private final int viewCount;
}
