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

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class ReviewResDto {
    private UUID writerId;
    private String writerEmail;
    private String writerNickname;
    private Integer rating;
    private String content;
    private String profileImageUrl;
    private LocalDateTime userCreatedAt;


    public static ReviewResDto fromEntity(User writer, Integer rating, String content) {
        return ReviewResDto.builder()
                .writerId(writer.getId())
                .writerEmail(writer.getEmail())
                .writerNickname(writer.getNickname())
                .rating(rating)
                .content(content)
                .profileImageUrl(writer.getPicture())
                .userCreatedAt(writer.getCreatedAt())
                .build();
    }
}
