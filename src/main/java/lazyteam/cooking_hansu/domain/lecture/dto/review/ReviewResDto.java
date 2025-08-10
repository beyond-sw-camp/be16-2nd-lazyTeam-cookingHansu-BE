package lazyteam.cooking_hansu.domain.lecture.dto.review;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class ReviewResDto {
    private LectureReview lectureReviewPage;
    private Lecture lecture;
    private User writer;
    private Integer rating;
    private String content;

    public static ReviewResDto fromEntity(User writer, Integer rating, String content) {
        return ReviewResDto.builder()
                .writer(writer)
                .rating(rating)
                .content(content)
                .build();
    }
}
