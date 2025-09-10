package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.lecture.dto.review.ReviewModifyDto;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 복합 unique
@Table(
        name = "lecture_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_review_lecture_writer",
                columnNames = {"lecture_id", "writer_id"}
        )
)

public class LectureReview extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)

    private User writer;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void modifyReview(ReviewModifyDto reviewModifyDto) {
        if(reviewModifyDto.getContent()!=null) {
            this.content = reviewModifyDto.getContent();
        }
        if(reviewModifyDto.getRating()!=null) {
            this.rating = reviewModifyDto.getRating();
        }

    }

}
