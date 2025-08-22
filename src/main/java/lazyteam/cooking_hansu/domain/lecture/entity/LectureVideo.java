package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;

import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;

import lombok.*;

@Entity
@Table(name = "lecture_video")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureVideo extends BaseIdAndTimeEntity {

    // 🔗 강의와 연결 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 512, nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private Boolean preview;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private Integer duration; // 초단위로 데이터 받을 예정 > 250 : 4분10초

}