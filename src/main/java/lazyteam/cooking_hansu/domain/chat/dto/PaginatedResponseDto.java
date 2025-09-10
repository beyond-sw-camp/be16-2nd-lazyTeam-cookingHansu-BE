package lazyteam.cooking_hansu.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaginatedResponseDto<T> {
    private List<T> data;           // 실제 데이터 배열
    private boolean hasNext;        // 다음 페이지 존재 여부
    private String nextCursor;      // 다음 페이지 요청용 cursor
}
