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
    private UUID userId;
    private String email;
    private String userNickname;
    private String profileImageUrl;
    private LocalDateTime userCreatedAt; 

//    답변
    private String answerName;
    private String answerStatus; //답변 검증용
    private String answerContent;
    private LocalDateTime answerCreatedAt;
    private LocalDateTime answerUpdatedAt;
    private UUID answerId;
    private UUID qnaId;
    private String answerProfileUrl;
    private LocalDateTime answerJoinedAt; // 자식 가입 일자
    private String answerEmail; // 자식 이메일

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
                .userId(parent.getUser() != null ? parent.getUser().getId() : null)
                .qnaId(parent.getId())
                .email(parent.getUser() != null ? parent.getUser().getEmail() : null)
                .userNickname(parent.getUser() != null ? parent.getUser().getNickname() : null)
                .profileImageUrl(parent.getUser() != null ? parent.getUser().getPicture() : null)
                .userCreatedAt(parent.getUser() != null ? parent.getUser().getCreatedAt() : null)

                // 답변
                .answerName(answer != null && answer.getUser() != null ? answer.getUser().getName() : null)
                .answerContent(answer != null ? answer.getContent() : null)
                .answerCreatedAt(answer != null ? answer.getCreatedAt() : null)
                .answerUpdatedAt(answer != null ? answer.getUpdatedAt() : null)
                .answerId(answer != null && answer.getUser() != null ? answer.getUser().getId(): null)
                .answerProfileUrl(answer != null && answer.getUser() != null ? answer.getUser().getPicture(): null)
                .answerJoinedAt(answer != null && answer.getUser() != null ? answer.getUser().getCreatedAt(): null)
                .answerEmail(answer != null && answer.getUser() != null ? answer.getUser().getEmail(): null)
                .build();
    }

}
