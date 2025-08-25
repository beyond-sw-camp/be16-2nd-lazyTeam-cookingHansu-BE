package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.lecture.dto.qna.QnaStatus;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private UUID parentId;

//    답변
    private String answerName;
    private String answerStatus; //답변 검증용
    private String answerContent;
    private LocalDateTime answerCreatedAt;
    private LocalDateTime answerUpdatedAt;
    private UUID answerId;

    public static QnaResDto fromEntity(LectureQna lectureQna) {

        LectureQna parent = (lectureQna.getParent() != null)
                ? lectureQna.getParent()
                : lectureQna;
        LectureQna answer = parent.getChild();

        return QnaResDto.builder()
                .parentName(parent.getUser() != null ? parent.getUser().getName() : null)
                .parentStatus(parent.getStatus())
                .parentContent(parent.getContent())
                .parentCreatedAt(parent.getCreatedAt())
                .questionUpdatedAt(parent.getUpdatedAt())
                .parentId(parent.getUser().getId())

                // 답변
                .answerName(answer != null && answer.getUser() != null ? answer.getUser().getName() : null)
                .answerContent(answer != null ? answer.getContent() : null)
                .answerCreatedAt(answer != null ? answer.getCreatedAt() : null)
                .answerUpdatedAt(answer != null ? answer.getUpdatedAt() : null)
                .answerId(answer != null && answer.getUser() != null ? answer.getUser().getId(): null)
                .build();
    }

}
