package lazyteam.cooking_hansu.domain.notification.dto;

import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lombok.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SseMessageDto {
    private UUID recipientId;
    private TargetType targetType;
    private UUID targetId;
    private String content;
}
