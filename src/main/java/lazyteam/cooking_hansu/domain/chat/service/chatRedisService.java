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
public class chatRedisService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;

    public chatRedisService(@Qualifier("chatPub") StringRedisTemplate stringRedisTemplate, SimpMessageSendingOperations messageTemplate) {
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
            // JSON 문자열을 ChatMessageResDto 객체로 변환
            ChatMessageResDto chatMessage = objectMapper.readValue(payload, ChatMessageResDto.class);

            // 채팅 메시지를 특정 주제에 발행
            messageTemplate.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // 예외 처리 로직 추가 가능
        }
    }
}
