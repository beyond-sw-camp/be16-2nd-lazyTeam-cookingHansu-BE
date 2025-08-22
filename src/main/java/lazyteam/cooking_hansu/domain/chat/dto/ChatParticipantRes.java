package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// TODO: 추후 확장성을 위해 제작함.

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatParticipantRes {
    private UUID id; // 사용자 ID
    private Long roomId; // 채팅방 ID
    private Long lastMessageId; // 마지막 메시지 ID

    public static ChatParticipantRes fromEntity(ChatParticipant chatParticipant) {
        return ChatParticipantRes.builder()
                .id(chatParticipant.getUser().getId())
                .roomId(chatParticipant.getChatRoom().getId())
                .lastMessageId(chatParticipant.getLastReadMessage() == null ? 0 : chatParticipant.getLastReadMessage().getId())
                .build();
    }
}
