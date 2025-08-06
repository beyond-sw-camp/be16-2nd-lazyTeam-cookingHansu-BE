package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;

import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void updateCustomRoomName(String customRoomName) {
        this.customRoomName = customRoomName;
    }
}
