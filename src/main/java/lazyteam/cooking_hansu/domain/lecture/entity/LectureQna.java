package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.LectureQnaUpdateDto;
import lazyteam.cooking_hansu.domain.lecture.dto.qna.QnaStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

@Entity
@Table(name = "lecture_qna")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureQna extends BaseIdAndTimeEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_parent_id")
    private LectureQna parent; // 부모 Q&A (답변의 경우)

    @OneToOne(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private LectureQna child; // 자식 Q&A (질문의 경우)
    // 강의 연관
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 질문 or 답변자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 질문자 또는 답변자

    // 질문 내용 or 답변 내용
    @Column(length = 255, nullable = false)
    private String content;

    // 질문 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private QnaStatus status = QnaStatus.PENDING;

    public void updateStatus(QnaStatus status) {
        this.status = status;
    }

    public void updateQnAText(LectureQnaUpdateDto lectureQnaUpdateDto){
        this.content = lectureQnaUpdateDto.getContent();
    }

    public void setChild(LectureQna child) {
        this.child = child;
        child.parent = this;
    }
}