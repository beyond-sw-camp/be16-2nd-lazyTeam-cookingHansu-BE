package lazyteam.cooking_hansu.domain.lecture.dto.review;

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

public class ReviewModifyDto extends BaseIdAndTimeEntity {
    private Integer rating;
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