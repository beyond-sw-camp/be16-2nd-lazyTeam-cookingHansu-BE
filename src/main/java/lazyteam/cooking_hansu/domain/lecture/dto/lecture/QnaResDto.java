package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.dto.qna.QnaStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QnaResDto {
//    부모
    private String parentName;
    private QnaStatus parentStatus; //답변 검증용
    private String parentContent;
    private LocalDateTime parentCreatedAt;
    private LocalDateTime questionUpdatedAt;

//    답변
    private String answerName;
    private String answerStatus; //답변 검증용
    private String answerContent;
    private LocalDateTime answerCreatedAt;
    private LocalDateTime answerUpdatedAt;

    public static QnaResDto fromEntity(LectureQna lectureQna) {
        LectureQna parent = lectureQna.getParent();
        LectureQna answer = lectureQna.getChild();

        return QnaResDto.builder()
                .parentName(parent.getUser().getName())
                .parentStatus(lectureQna.getStatus())
                .parentContent(lectureQna.getContent())
                .parentCreatedAt(parent.getCreatedAt())
                .questionUpdatedAt(parent.getUpdatedAt())

                // 답변
                .answerName(answer.getUser().getName())
                .answerContent(lectureQna.getChild().getContent())
                .answerCreatedAt(lectureQna.getChild().getCreatedAt())
                .answerUpdatedAt(lectureQna.getUpdatedAt())
                .build();
    }

}
