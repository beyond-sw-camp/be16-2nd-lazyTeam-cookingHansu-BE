package lazyteam.cooking_hansu.domain.chat.dto;

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
public class MyChatListDto {
    private UUID chatRoomId; // 채팅방 ID
    private String customRoomName; // 커스텀 채팅방 이름
    private String otherUserName; // 상대방 이름
    private String otherUserNickname; // 상대방 닉네임
    private String otherUserProfileImage; // 상대방 프로필 이미지 URL
    private String lastMessage; // 마지막 메시지 내용
    private LocalDateTime lastMessageTime; // 마지막 메시지 시간
    private Integer unreadCount; // 읽지 않은 메시지 수
}
