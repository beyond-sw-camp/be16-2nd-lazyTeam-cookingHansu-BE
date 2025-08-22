package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.entity.LectureStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class LectureStepResDto {
    private Integer stepSequence;
    private String content;

    public static LectureStepResDto fromEntity(LectureStep lectureStep) {
        return LectureStepResDto.builder()
                .stepSequence(lectureStep.getStepSequence())
                .content(lectureStep.getContent())
                .build();
    }
}
