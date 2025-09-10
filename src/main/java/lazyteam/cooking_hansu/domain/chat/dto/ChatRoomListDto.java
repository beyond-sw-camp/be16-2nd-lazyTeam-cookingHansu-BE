package lazyteam.cooking_hansu.domain.chat.dto;

import lazyteam.cooking_hansu.domain.chat.entity.ChatParticipant;
import lazyteam.cooking_hansu.domain.chat.entity.ChatRoom;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatRoomListDto {
    private Long roomId; // 채팅방 ID
    private String customRoomName; // 커스텀 채팅방 이름
    private String otherUserName; // 상대방 이름
    private String otherUserNickname; // 상대방 닉네임
    private String otherUserProfileImage; // 상대방 프로필 이미지 URL
    private String lastMessage; // 마지막 메시지 내용
    private Integer newMessageCount; // 읽지 않은 메시지 수
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간

    public static ChatRoomListDto fromEntity(ChatRoom chatRoom, User otherUser, int unreadCount, ChatParticipant participant) {
        String lastMessage = "메세지를 보내 채팅을 시작해보세요!";
        if (!chatRoom.getMessages().isEmpty()) {
            lastMessage = chatRoom.getMessages().get(chatRoom.getMessages().size() - 1).getMessageText();
        }

        return ChatRoomListDto.builder()
                .roomId(chatRoom.getId())
                .customRoomName(participant.getCustomRoomName())
                .otherUserName(otherUser.getName())
                .otherUserNickname(otherUser.getNickname())
                .otherUserProfileImage(otherUser.getPicture())
                .lastMessage(lastMessage)
                .newMessageCount(unreadCount)
                .lastMessageTime(chatRoom.getMessages().isEmpty() ? null : chatRoom.getMessages().get(chatRoom.getMessages().size() - 1).getCreatedAt())
                .build();
    }
}
