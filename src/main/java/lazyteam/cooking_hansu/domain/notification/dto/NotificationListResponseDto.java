package lazyteam.cooking_hansu.domain.notification.dto;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NotificationListResponseDto {
    private List<NotificationDto> notifications;
    private String nextCursor;  // 다음 페이지 요청을 위한 cursor
    private boolean hasNext;    // 다음 페이지 존재 여부
}
