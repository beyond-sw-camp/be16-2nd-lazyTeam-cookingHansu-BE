package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
//ERD상에서 조리순서(내레시피)가 두개라서 일단 클래스명 이렇게 명시했음.
public class LectureStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stepId;

    // FK: 강의
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    // 조리 순서 번호
    @Column(nullable = false)
    private Integer stepSequence;

    // 조리 내용
    @Column(length = 255, nullable = false)
    private String content;
}
