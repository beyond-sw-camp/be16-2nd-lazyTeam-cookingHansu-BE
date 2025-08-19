package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class LectureVideoResDto {

    private String title;
    private String videoUrl;
    private Boolean preview;
    private Integer sequence;
    private Integer duration; // 초단위로 데이터 받을 예정 > 250 : 4분10초

    public static LectureVideoResDto fromEntity(LectureVideo lectureVideo) {
        return LectureVideoResDto.builder()
                .title(lectureVideo.getTitle())
                .videoUrl(lectureVideo.getVideoUrl())
                .preview(lectureVideo.getPreview())
                .sequence(lectureVideo.getSequence())
                .duration(lectureVideo.getDuration())
                .build();
    }
}
