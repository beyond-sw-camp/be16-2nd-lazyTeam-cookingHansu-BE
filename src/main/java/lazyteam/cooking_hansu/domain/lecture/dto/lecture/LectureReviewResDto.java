package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LectureReviewResDto {

    private String writer;
    private Integer rating;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private UUID reviewerId;
    private String profileUrl;
    private LocalDateTime reviewerJoinedAt; //가입일자
    private String reviewerEmail; // 이메일

    public static LectureReviewResDto fromEntity(LectureReview lectureReview) {
        return LectureReviewResDto.builder()
                .writer(lectureReview.getWriter().getName())
                .rating(lectureReview.getRating())
                .content(lectureReview.getContent())
                .createAt(lectureReview.getCreatedAt())
                .updateAt(lectureReview.getUpdatedAt())
                .reviewerId(lectureReview.getWriter().getId())
                .profileUrl(lectureReview.getWriter().getPicture())
                .reviewerJoinedAt(lectureReview.getWriter().getCreatedAt())
                .reviewerEmail(lectureReview.getWriter().getEmail())
                .build();
    }
}
