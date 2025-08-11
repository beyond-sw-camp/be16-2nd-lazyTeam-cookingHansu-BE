package lazyteam.cooking_hansu.domain.chat.controller;

import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageReqDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantStatReq;
import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lazyteam.cooking_hansu.domain.chat.service.ChatRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;
    private final ChatRedisService chatRedisService;

    @MessageMapping("/chat-rooms/{roomId}/message")
    public void sendMessage(@DestinationVariable UUID roomId, ChatMessageReqDto messageReqDto) {
        ChatMessageResDto saved = chatService.saveMessage(roomId, messageReqDto);
        chatRedisService.publishChatMessageToRedis(roomId, saved);
    }

    @MessageMapping("/chat-rooms/{roomId}/online")
    public void online(@DestinationVariable UUID roomId, ChatParticipantStatReq req) {
        chatRedisService.publishChatOnlineToRedis(roomId, req);
    }

    @MessageMapping("/chat-rooms/{roomId}/offline")
    public void offline(@DestinationVariable UUID roomId, ChatParticipantStatReq req) {
        // 오프라인 처리 시 읽음 반영이 필요하면 유지
        chatService.readMessages(roomId, req.getUserId());
        chatRedisService.publishChatOfflineToRedis(roomId, req);
    }
}
