package lazyteam.cooking_hansu.domain.interaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.interaction.entity.LectureLikes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자가 좋아요한 강의 목록 응답 DTO
 * 마이페이지 등에서 사용자가 좋아요한 강의 목록을 보여줄 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자가 좋아요한 강의 목록 응답")
public class UserLikedLectureDto {

    @Schema(description = "강의 ID")
    private UUID lectureId;

    @Schema(description = "강의 제목", example = "집에서 만드는 김치찌개")
    private String title;

    @Schema(description = "강의 설명", example = "초보자도 쉽게 만들 수 있는 김치찌개")
    private String description;

    @Schema(description = "강의 썸네일 URL")
    private String thumbUrl;

    @Schema(description = "강의 가격", example = "15000")
    private Integer price;

    @Schema(description = "강의 난이도")
    private LevelEnum level;

    @Schema(description = "강의 카테고리")
    private CategoryEnum category;

    @Schema(description = "강의 작성자", example = "김셰프")
    private String instructorName;

    @Schema(description = "좋아요 누른 시간")
    private LocalDateTime likedAt;

    @Schema(description = "총 좋아요 수", example = "128")
    private Long totalLikes;

    public static UserLikedLectureDto fromEntity(LectureLikes lectureLike, Long totalLikes) {
        return UserLikedLectureDto.builder()
                .lectureId(lectureLike.getLecture().getId())
                .title(lectureLike.getLecture().getTitle())
                .description(lectureLike.getLecture().getDescription())
                .thumbUrl(lectureLike.getLecture().getThumbUrl())
                .price(lectureLike.getLecture().getPrice())
                .level(lectureLike.getLecture().getLevel())
                .category(lectureLike.getLecture().getCategory())
                .instructorName(lectureLike.getLecture().getSubmittedBy().getName())
                .likedAt(lectureLike.getCreatedAt())
                .totalLikes(totalLikes)
                .build();
    }
}
