package lazyteam.cooking_hansu.domain.comment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PostCommentUpdateDto {
    @NotNull(message = "댓글 내용은 필수입니다")
    @Size(max = 500, message = "댓글 내용은 500자 이하여야 합니다")
    private String content;
}
