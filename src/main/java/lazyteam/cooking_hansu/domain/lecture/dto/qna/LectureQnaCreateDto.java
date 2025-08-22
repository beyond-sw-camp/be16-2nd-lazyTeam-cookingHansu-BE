package lazyteam.cooking_hansu.domain.lecture.dto.qna;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LectureQnaCreateDto {
    @NotNull(message = "강의 질문 내용은 필수입니다")
    private String content; // 질문 내용
    private UUID parentId; // 부모 질문 ID (답변일 경우)
}
