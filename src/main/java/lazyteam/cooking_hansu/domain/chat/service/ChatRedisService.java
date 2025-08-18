package lazyteam.cooking_hansu.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lazyteam.cooking_hansu.domain.chat.dto.ChatMessageResDto;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantAddRes;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantRes;
import lazyteam.cooking_hansu.domain.chat.dto.ChatParticipantStatReq;
import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.repository.ChatParticipantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class ChatRedisService implements MessageListener {

    private final ChatParticipantRepository chatParticipantRepository;
    private final RedisTemplate<String, String> chatPubsubRedisTemplate;
    private final SimpMessageSendingOperations messageTemplate;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Map<String, String>> chatParticipantsRedisTemplate;
    private final RedisTemplate<String, String> chatOnlineParticipantRedisTemplate;


    // Redis Keys
    private static final String ONLINE_KEY_PREFIX = "chat-online_";
    private static final String PARTICIPANTS_KEY_PREFIX = "chat-participants_";

    // STOMP Topics
    private static final String TOPIC_PREFIX = "/topic/chat-rooms/";
    private static final String SUFFIX_CHAT_MESSAGE = "/chat-message";
    private static final String SUFFIX_ONLINE_PARTICIPANT = "/online-participant";
    private static final String SUFFIX_CHAT_PARTICIPANTS = "/chat-participants";

    public ChatRedisService(
            @Qualifier("chatPubSub") RedisTemplate<String, String> chatPubsubRedisTemplate,
            @Qualifier("chatParticipants") RedisTemplate<String, Map<String, String>> chatParticipantsRedisTemplate,
            @Qualifier("chatOnlineParticipants") RedisTemplate<String, String> chatOnlineParticipantRedisTemplate,
            SimpMessageSendingOperations messageTemplate,
            ChatParticipantRepository chatParticipantRepository
    ) {
        this.chatParticipantRepository = chatParticipantRepository;
        this.chatPubsubRedisTemplate = chatPubsubRedisTemplate;
        this.messageTemplate = messageTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.chatParticipantsRedisTemplate = chatParticipantsRedisTemplate;
        this.chatOnlineParticipantRedisTemplate = chatOnlineParticipantRedisTemplate;
    }

    /* ----------------------------- Publish (to Redis) ----------------------------- */

    public void publishChatMessageToRedis(UUID roomId, ChatMessageResDto chatMessage) {
        Set<String> onlineParticipants = chatOnlineParticipantRedisTemplate.opsForSet().members(ONLINE_KEY_PREFIX + roomId);
        Map<String, String> chatParticipants = getOrLoadParticipantMap(roomId);

        onlineParticipants.forEach(onlineParticipant -> {
            chatParticipants.put(onlineParticipant, String.valueOf(chatMessage.getId()));
        });

        chatParticipantsRedisTemplate.opsForValue().set(PARTICIPANTS_KEY_PREFIX + roomId, chatParticipants);

        String payload = null;
        try {
            payload = objectMapper.writeValueAsString(chatMessage);
            log.info("Publishing chat message to Redis: roomId={}, payload={}", roomId, payload);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류: {}", e.getMessage());
            throw new IllegalStateException("Failed to serialize chat message", e);
        }

        chatPubsubRedisTemplate.convertAndSend(topic(roomId, SUFFIX_CHAT_MESSAGE), payload);
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

        List<ChatParticipantStatReq> list = new ArrayList<>();
        if(online !=null){
            online.forEach(userId -> {
                list.add(ChatParticipantStatReq.builder().userId(UUID.fromString(userId)).build());
            });
        }

        try {
            String message = objectMapper.writeValueAsString(list); // JSON 배열
            chatPubsubRedisTemplate.convertAndSend(topic(roomId, SUFFIX_ONLINE_PARTICIPANT), message);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류: {}", e.getMessage());
            throw new IllegalStateException("Failed to serialize online participants", e);
        }
    }

    public Map<String, String> getOrLoadParticipantMap(UUID roomId) {
        Map<String, String> participantMap = chatParticipantsRedisTemplate.opsForValue().get(PARTICIPANTS_KEY_PREFIX + roomId);

        if (participantMap == null || participantMap.isEmpty()) {
            // 레디스에 없으면 db에서 조회해서 map을 만들어 줌
            List<ChatParticipant> mariaDBChatParticipantList = chatParticipantRepository.findAllByChatRoomId(roomId);

            participantMap = new HashMap<>();

            for (ChatParticipant chatParticipant : mariaDBChatParticipantList) {
                UUID lastReadChatMessageId = chatParticipant.getLastReadMessage() == null ? UUID.fromString("12345678-0000-0000-0000-000000000000") : chatParticipant.getLastReadMessage().getId();
                participantMap.put(String.valueOf(chatParticipant.getUser().getId()), String.valueOf(lastReadChatMessageId));
            }
        }
        return participantMap;
    }

    // TODO: 확장성을 위해 추가한 메소드
    // redis의 참여자 목록에서 제거하고 publish
    public void publishLeftUserToRedis(UUID roomId) {
        Map<String, String> redisChatParticipantMap = getOrLoadParticipantMap(roomId);

        redisChatParticipantMap.remove(SecurityContextHolder.getContext().getAuthentication().getName());
        chatParticipantsRedisTemplate.opsForValue().set(PARTICIPANTS_KEY_PREFIX + roomId, redisChatParticipantMap);

        chatPubsubRedisTemplate.convertAndSend(TOPIC_PREFIX + roomId + SUFFIX_CHAT_PARTICIPANTS, toJson(roomId, redisChatParticipantMap));
    }

    // TODO: 확장성을 위해 추가한 메소드
    // redis의 참여자 목록에 추가하고 publish
    public void publishInvitedUsersToRedis(UUID roomId, List<ChatParticipantAddRes> chatParticipantAddResList) {
        Map<String, String> redisChatParticipantMap = getOrLoadParticipantMap(roomId);

        for (ChatParticipantAddRes chatParticipantAddRes : chatParticipantAddResList) {
            redisChatParticipantMap.put(String.valueOf(chatParticipantAddRes.getId()), "0");
        }

        chatParticipantsRedisTemplate.opsForValue().set(PARTICIPANTS_KEY_PREFIX + roomId, redisChatParticipantMap);
        chatPubsubRedisTemplate.convertAndSend(TOPIC_PREFIX + roomId + SUFFIX_CHAT_PARTICIPANTS, toJson(roomId, redisChatParticipantMap));
    }

    // TODO: 확장성을 위해 추가한 메소드
    private String toJson(UUID roomId, Map<String, String> chatParticipantsMap) {
        List<ChatParticipantRes> chatParticipantResList = new ArrayList<>();

        for (Map.Entry<String, String> entry : chatParticipantsMap.entrySet()) {
            chatParticipantResList.add(
                    ChatParticipantRes.builder()
                            .id(UUID.fromString(entry.getKey()))
                            .roomId(roomId)
                            .lastMessageId(UUID.fromString(entry.getValue()))
                            .build()
            );
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String data = null;
        try {
            data = objectMapper.writeValueAsString(chatParticipantResList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return data;
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
                List<ChatParticipantStatReq> onlineParticipants = new ArrayList<>();
                JsonNode jsonNode = objectMapper.readTree(body);
                for(JsonNode node : jsonNode) {
                    ChatParticipantStatReq participant = objectMapper.treeToValue(node, ChatParticipantStatReq.class);
                    onlineParticipants.add(participant);
                }
                messageTemplate.convertAndSend(dest, onlineParticipants);
            } else if (dest.endsWith(SUFFIX_CHAT_PARTICIPANTS)) {
                List<ChatParticipantRes> chatParticipants = new ArrayList<>();
                JsonNode jsonNode = objectMapper.readTree(body);
                for (JsonNode node : jsonNode) {
                    ChatParticipantRes participant = objectMapper.treeToValue(node, ChatParticipantRes.class);
                    chatParticipants.add(participant);
                }
                // Set<String>를 JSON 문자열로 전달 (클라에서 그대로 파싱)
                messageTemplate.convertAndSend(dest, chatParticipants);
            } else {
                log.warn("Unknown channel: {}", channel);
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
}
