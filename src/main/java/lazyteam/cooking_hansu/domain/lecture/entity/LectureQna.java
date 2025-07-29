package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lecture_qna")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureQna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    // 강의 연관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 질문자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_user_id", nullable = false)
    private User questionUser;

    // 답변자 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_user_id")
    private User answerUser;

    // 질문 내용
    @Column(length = 255, nullable = false)
    private String questionText;

    //  질문 작성 시간
    @Column(nullable = false)
    private String questionCreatedAt;

    //  답변 내용 (nullable)
    @Column(length = 255)
    private String answerText;

    // 답변 시간 (nullable)
    private String answerCreatedAt;
}