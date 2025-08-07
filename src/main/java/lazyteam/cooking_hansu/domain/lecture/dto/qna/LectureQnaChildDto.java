package lazyteam.cooking_hansu.domain.lecture.dto.qna;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LectureQnaChildDto {
    private UUID id; // 강의 질문 ID
    private UUID parentId; // 부모 질문 ID (답변일 경우)
    private String content; // 질문 내용
    private String userName; // 질문 작성자 이름
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
}
