package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LectureReviewResDto {

    private String writer;
    private Integer rating;
    private String content;

    public static LectureReviewResDto fromEntity(LectureReview lectureReview) {
        return LectureReviewResDto.builder()
                .writer(lectureReview.getWriter().getName())
                .rating(lectureReview.getRating())
                .content(lectureReview.getContent())
                .build();
    }
}
