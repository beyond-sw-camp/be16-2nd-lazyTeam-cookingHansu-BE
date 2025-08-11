package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;

import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class ChatParticipant extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 참여자 정보

    @Column(name = "custom_room_name", length = 100)
    private String customRoomName; // 이 유저에게 보이는 채팅방 이름

    @Column(name = "is_active")
    @Builder.Default
    private String isActive = "Y"; // 채팅방 참여 상태

    @Column(name = "left_at")
    private LocalDateTime leftAt; // 나간 시간 기록

    @ManyToOne
    @JoinColumn(name = "chat_message_id")
    private ChatMessage lastReadMessage;

    public void updateCustomRoomName(String customRoomName) {
        this.customRoomName = customRoomName;
    }

    public void leaveChatRoom() {
        this.isActive = "N"; // 채팅방 참여 상태를 비활성화로 변경
        this.leftAt = LocalDateTime.now(); // 나간 시간을 현재 시간으로 설정
    }
    public void joinChatRoom() {
        this.isActive = "Y"; // 채팅방 참여 상태를 활성화로 변경
    }

    public void read(ChatMessage lastReadMessage) {
        this.lastReadMessage = lastReadMessage;
    }

}
