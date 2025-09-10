package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class LectureStepDto {
    private Integer stepSequence;
    private String content;

    public LectureStep toEntity(Lecture lecture) {
        return LectureStep.builder()
                .content(content)
                .lecture(lecture)
                .stepSequence(stepSequence)
                .build();
    }
}