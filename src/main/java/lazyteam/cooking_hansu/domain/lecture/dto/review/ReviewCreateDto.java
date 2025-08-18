package lazyteam.cooking_hansu.domain.lecture.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder

public class ReviewCreateDto extends BaseIdAndTimeEntity {
    @NotNull(message = "평점을 입력해주세요.")
    private Integer rating;
    @NotBlank(message = "내용을 작성해주세요.")
    private String content;
    @NotNull(message = "강의 ID가 없습니다.")
    private UUID lectureId;

    public LectureReview toEntity(Lecture lecture, User user) {
        return LectureReview.builder()
                .lecture(lecture)
                .writer(user)
                .content(content)
                .rating(rating)
                .build();
    }

}