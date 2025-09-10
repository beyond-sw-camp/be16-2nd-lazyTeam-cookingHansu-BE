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
public class LectureQnaUpdateDto {
    @NotNull(message = "내용 입력은 필수입니다")
    private String content; // 답변 내용
}
