package lazyteam.cooking_hansu.domain.lecture.dto;


import jakarta.persistence.*;
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

public class LectureVideoDto {


    private Lecture lecture;

    private String title;

    private String videoUrl;

    private Boolean preview;

    private Integer sequence;

    private Integer duration; // 초단위로 데이터 받을 예정 > 250 : 4분10초


    public LectureVideo toEntity(Lecture lecture, String videoUrl, int duration, boolean preview) {
        return LectureVideo.builder()
                .lecture(lecture)
                .title(title)
                .videoUrl(videoUrl)
                .duration(duration)
                .preview(preview)
                .sequence(sequence)
                .build();
    }


}
