package lazyteam.cooking_hansu.domain.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageTextDto;
import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lazyteam.cooking_hansu.domain.chat.service.RedisPubSubService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;
    private final RedisPubSubService redisPubSubService;


    @MessageMapping("/{roomId}") // 클라이언트에서 특정 Publish/{roomId}로 메시지를 발행하면 해당 메소드가 호출됨
    @SendTo("/topic/{roomId}") // 해당 roomId에 메시지를 발행하여 구독중인 클라이언트에게 메시지 전송
//    destinationVariable은 @MessageMapping 어노테이션으로 정의된 WebSocket Controller 내에서만 사용
    public void sendMessage(@DestinationVariable UUID roomId, ChatMessageTextDto messageReqDto) throws JsonProcessingException {
        chatService.saveMessage(roomId, messageReqDto);
        messageReqDto.setRoomId(roomId);

        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(messageReqDto);
        redisPubSubService.publish("chat", message);
    }
}
