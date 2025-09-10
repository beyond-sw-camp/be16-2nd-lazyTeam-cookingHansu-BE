package lazyteam.cooking_hansu.domain.notification.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lazyteam.cooking_hansu.domain.notification.dto.ChatNotificationDto;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisher {
    public static final String CHANNEL = "notify-channel";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationPublisher(
            @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(SseMessageDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.convertAndSend(CHANNEL, json);
        } catch (Exception ignored) {}
    }

    public void publishChatNotification(ChatNotificationDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.convertAndSend(CHANNEL, json); // 같은 채널 사용
        } catch (Exception ignored) {}
    }
}
