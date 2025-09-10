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

    private UUID writerId;
    private String writerEmail;
    private String writerNickname;
    private String profileImageUrl;
    private LocalDateTime userCreatedAt;
    private String writerName;
    private Integer rating;
    private String content;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;


    public static LectureReviewResDto fromEntity(LectureReview lectureReview) {
        return LectureReviewResDto.builder()
                .writerName(lectureReview.getWriter().getName())
                .rating(lectureReview.getRating())
                .content(lectureReview.getContent())
                .createAt(lectureReview.getCreatedAt())
                .updateAt(lectureReview.getUpdatedAt())
                .writerId(lectureReview.getWriter().getId())
                .writerEmail(lectureReview.getWriter().getEmail())
                .writerNickname(lectureReview.getWriter().getNickname())
                .profileImageUrl(lectureReview.getWriter().getPicture())
                .userCreatedAt(lectureReview.getWriter().getCreatedAt())
                .build();
    }
}
