package lazyteam.cooking_hansu.domain.lecture.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LectureQnaCreateDto {
    @NotNull(message = "강의 질문 제목은 필수입니다")
    private String title; // 질문 제목
    @NotNull(message = "강의 질문 내용은 필수입니다")
    private String questionText; // 질문 내용
}
