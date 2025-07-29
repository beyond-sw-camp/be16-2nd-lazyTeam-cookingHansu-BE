package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity

@Getter

@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LectureReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

}
