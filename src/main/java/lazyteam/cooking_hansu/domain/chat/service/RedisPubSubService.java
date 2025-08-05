package lazyteam.cooking_hansu.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;

    public RedisPubSubService(@Qualifier("chatPub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody());
        try {
            ChatMessageResDto chatMessageResDto = objectMapper.readValue(payload, ChatMessageResDto.class);
            messageTemplate.convertAndSend("/topic/" + chatMessageResDto.getRoomId(), chatMessageResDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
