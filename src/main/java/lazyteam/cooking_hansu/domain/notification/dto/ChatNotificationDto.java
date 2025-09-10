package lazyteam.cooking_hansu.domain.notification.dto;

import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatNotificationDto {
    private UUID recipientId;        // 수신자 ID
    private Long chatRoomId;         // 채팅방 ID
    private UUID targetId;           // 알림 대상 ID (UUID)
    private String content;          // 채팅 내용
    private UUID senderId;           // 발신자 ID
    private TargetType targetType;   // 알림 타입
    private LocalDateTime createdAt; // 발송 시간
}
