package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class ChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 채팅방 ID

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 채팅방 이름

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>(); // 채팅 참여자 목록

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>(); // 채팅 메시지 목록
}
