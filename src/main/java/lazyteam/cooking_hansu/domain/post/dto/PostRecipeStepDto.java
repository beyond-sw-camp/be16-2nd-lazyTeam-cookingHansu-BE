package lazyteam.cooking_hansu.domain.post.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PostRecipeStepDto {
    private UUID stepId;
    private String content;
}