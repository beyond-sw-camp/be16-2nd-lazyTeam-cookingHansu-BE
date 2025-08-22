package lazyteam.cooking_hansu.domain.chat.controller;

import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageReqDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantStatReq;
import lazyteam.cooking_hansu.domain.chat.service.ChatService;
import lazyteam.cooking_hansu.domain.chat.service.ChatRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;
    private final ChatRedisService chatRedisService;

    @MessageMapping("/chat-rooms/{roomId}/chat-message")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageReqDto messageReqDto) {
        ChatMessageResDto saved = chatService.saveMessage(roomId, messageReqDto);

        // 2. 재활성화된 참여자들을 Redis에 추가
        addReactivatedParticipantsToRedis(roomId, messageReqDto.getSenderId());

        chatRedisService.publishChatMessageToRedis(roomId, saved);
    }

//    @MessageMapping("/chat-rooms/{roomId}/invite")
//    public void inviteUsers(@DestinationVariable Long roomId, List<ChatParticipantStatReq> chatParticipantAddReqs) {
//        List<ChatParticipantStatReq> chatParticipantAddRes = chatService.inviteUsers(roomId, chatParticipantAddReqs);
//        // Redis에 초대된 참여자 정보를 발행
//        chatRedisService.publishInvitedUsersToRedis(roomId, chatParticipantAddRes);
//    }
//
//    @MessageMapping("/chat-rooms/{roomId}/leave")
//    public void leaveRoom(@DestinationVariable Long roomId) {
//        chatService.leaveChatRoomAndRemoveIfEmpty(roomId);
//        // Redis에 참여자 퇴장 정보를 발행
//        chatRedisService.publishLeftUserToRedis(roomId);
//    }

    @MessageMapping("/chat-rooms/{roomId}/online")
    public void online(@DestinationVariable Long roomId, ChatParticipantStatReq req) {
        chatRedisService.publishChatOnlineToRedis(roomId, req);
    }

    @MessageMapping("/chat-rooms/{roomId}/offline")
    public void offline(@DestinationVariable Long roomId, ChatParticipantStatReq req) {
        chatService.readMessages(roomId, req.getUserId());
        chatRedisService.publishChatOfflineToRedis(roomId, req);
    }

    private void addReactivatedParticipantsToRedis(Long roomId, UUID senderId) {
        try {
            // ChatService에서 재활성화된 참여자 정보를 가져와서 Redis에 추가
            List<UUID> reactivatedUsers = chatService.getReactivatedParticipants(roomId, senderId);

            for (UUID userId : reactivatedUsers) {
                chatRedisService.addUserToRoom(roomId, userId);
            }

            if (!reactivatedUsers.isEmpty()) {
                log.info("Added {} reactivated participants to Redis for room {}", reactivatedUsers.size(), roomId);
            }

        } catch (Exception e) {
            log.error("Failed to add reactivated participants to Redis: {}", e.getMessage());
        }
    }
}
