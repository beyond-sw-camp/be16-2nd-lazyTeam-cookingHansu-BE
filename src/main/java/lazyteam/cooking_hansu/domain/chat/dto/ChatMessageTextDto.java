package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.ChatMessage;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatMessageTextDto {
    private UUID roomId; // 채팅방 ID
    private UUID senderId; // 발신자 ID
    private String message; // 메시지 내용


    public static ChatMessageTextDto fromEntity(ChatMessage chatMessage, User user) {
        return ChatMessageTextDto.builder()
                .roomId(chatMessage.getChatRoom().getId())
                .senderId(user.getId())
                .message(chatMessage.getMessageText())
                .build();
    }
}
