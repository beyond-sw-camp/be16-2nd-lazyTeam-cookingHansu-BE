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
public class LectureQnaListDto {
    private UUID id; // 강의 질문 ID
    private String content; // 질문 내용
    private QnaStatus status; // 질문 상태 (답변 대기, 답변 완료)
    private String userName; // 질문 작성자 이름
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
    private List<LectureQnaChildDto> answers; // 답변 목록 (자식 질문들)

}
