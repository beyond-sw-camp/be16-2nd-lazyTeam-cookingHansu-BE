package lazyteam.cooking_hansu.domain.notification.pubsub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lazyteam.cooking_hansu.domain.notification.dto.ChatNotificationDto;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {
    private final SseEmitterRegistry registry;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            JsonNode jsonNode = objectMapper.readTree(messageBody);
            
            // 메시지 타입을 구분하여 처리
            if (jsonNode.has("chatRoomId")) {
                // 채팅 알림 (ChatNotificationDto) - chatRoomId 필드가 있으면
                ChatNotificationDto dto = objectMapper.readValue(messageBody, ChatNotificationDto.class);
                registry.send(dto.getRecipientId(), dto);
            } else {
                // 일반 알림 (SseMessageDto) - chatRoomId 필드가 없으면
                SseMessageDto dto = objectMapper.readValue(messageBody, SseMessageDto.class);
                registry.send(dto.getRecipientId(), dto);
            }
        } catch (Exception ignored) {}
    }
}
