package lazyteam.cooking_hansu.domain.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 강의 좋아요 정보 응답 DTO
 * 특정 강의의 좋아요 개수와 사용자의 좋아요 상태를 담는 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "강의 좋아요 정보 응답")
public class LectureLikeInfoDto {

    @Schema(description = "강의 ID")
    private UUID lectureId;

    @Schema(description = "좋아요 개수", example = "42")
    private Long likeCount;

    @Schema(description = "사용자의 좋아요 여부", example = "true")
    private Boolean isLiked;
}
