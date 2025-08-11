package lazyteam.cooking_hansu.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantStatReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ChatRedisService implements MessageListener {

    private final RedisTemplate<String, String> pubsubRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;            // STOMP bridge
    private final ObjectMapper objectMapper;                               // shared mapper
    private final RedisTemplate<String, String> chatOnlineParticipantRedisTemplate; // Set ops (online users)

    // Redis Keys
    private static final String ONLINE_KEY_PREFIX      = "chat-online_";       // SADD/SMEMBERS
    private static final String PARTICIPANT_KEY_PREFIX = "chat-participant_";  // HSET userId -> lastMsgId

    // STOMP Topics
    private static final String TOPIC_PREFIX = "/topic/chat-rooms/";
    private static final String SUFFIX_CHAT_MESSAGE       = "/chat-message";
    private static final String SUFFIX_ONLINE_PARTICIPANT = "/online-participant";

    public ChatRedisService(
            @Qualifier("chatPubSub") RedisTemplate<String, String> pubsubRedisTemplate,
            SimpMessageSendingOperations messageTemplate,
            @Qualifier("chatParticipant") RedisTemplate<String, String> chatOnlineParticipantRedisTemplate
    ) {
        this.pubsubRedisTemplate = pubsubRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.chatOnlineParticipantRedisTemplate = chatOnlineParticipantRedisTemplate;
    }

    /* ----------------------------- Publish (to Redis) ----------------------------- */

    // 메시지 발행: 온라인 사용자 읽음 포인터 갱신(HSET putAll) -> Redis Pub/Sub으로 전파
    public void publishChatMessageToRedis(UUID roomId, ChatMessageResDto chatMessage) {
        Set<String> onlineUserIds = chatOnlineParticipantRedisTemplate.opsForSet().members(onlineKey(roomId));

        if (onlineUserIds != null && !onlineUserIds.isEmpty()) {
            Map<String, String> updates = new HashMap<>(onlineUserIds.size());
            for (String userId : onlineUserIds) {
                updates.put(userId, chatMessage.getId().toString()); // userId -> lastMsgId
            }
            chatOnlineParticipantRedisTemplate.opsForHash().putAll(participantKey(roomId), updates);
        }

        final String payload;
        try {
            payload = objectMapper.writeValueAsString(chatMessage);
        } catch (JsonProcessingException e) {
            log.error("serialize error, roomId={}, msgId={}", roomId, chatMessage.getId(), e);
            throw new IllegalStateException("Failed to serialize ChatMessageResDto", e);
        }

        pubsubRedisTemplate.convertAndSend(topic(roomId, SUFFIX_CHAT_MESSAGE), payload);
    }

    // 온라인 진입 브로드캐스트(전체 온라인 목록 전송)
    public void publishChatOnlineToRedis(UUID roomId, ChatParticipantStatReq req) {
        chatOnlineParticipantRedisTemplate.opsForSet().add(onlineKey(roomId), String.valueOf(req.getUserId()));
        broadcastOnlineSet(roomId);
    }

    // 오프라인 브로드캐스트(전체 온라인 목록 전송)
    public void publishChatOfflineToRedis(UUID roomId, ChatParticipantStatReq req) {
        chatOnlineParticipantRedisTemplate.opsForSet().remove(onlineKey(roomId), String.valueOf(req.getUserId()));
        broadcastOnlineSet(roomId);
    }

    private void broadcastOnlineSet(UUID roomId) {
        Set<String> online = chatOnlineParticipantRedisTemplate.opsForSet().members(onlineKey(roomId));
        try {
            String message = objectMapper.writeValueAsString(online); // JSON 배열
            pubsubRedisTemplate.convertAndSend(topic(roomId, SUFFIX_ONLINE_PARTICIPANT), message);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류: {}", e.getMessage());
            throw new IllegalStateException("Failed to serialize online participants", e);
        }
    }

    /* ----------------------------- Redis -> STOMP Bridge ----------------------------- */

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        String dest = normalizeChannel(channel); // 끝 슬래시 제거

        try {
            if (dest.endsWith(SUFFIX_CHAT_MESSAGE)) {
                ChatMessageResDto dto = objectMapper.readValue(body, ChatMessageResDto.class);
                messageTemplate.convertAndSend(dest, dto);
            } else if (dest.endsWith(SUFFIX_ONLINE_PARTICIPANT)) {
                // Set<String>를 JSON 문자열로 전달 (클라에서 그대로 파싱)
                messageTemplate.convertAndSend(dest, body);
            } else {
                log.warn("Unhandled redis channel: {}", channel);
            }
        } catch (Exception e) {
            log.error("onMessage handling failed. channel={}, body={}", channel, body, e);
        }
    }

    /* ----------------------------- Utils ----------------------------- */

    private static String normalizeChannel(String ch) {
        if (ch == null || ch.isEmpty()) return ch;
        int len = ch.length();
        return (len > 1 && ch.charAt(len - 1) == '/') ? ch.substring(0, len - 1) : ch;
    }

    private static String topic(UUID roomId, String suffix) {
        return TOPIC_PREFIX + roomId + suffix; // 끝 슬래시 없음
    }

    private static String onlineKey(UUID roomId) {
        return ONLINE_KEY_PREFIX + roomId;
    }

    private static String participantKey(UUID roomId) {
        return PARTICIPANT_KEY_PREFIX + roomId;
    }
}
