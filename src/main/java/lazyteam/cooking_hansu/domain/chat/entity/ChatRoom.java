package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
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
public class ChatRoom extends BaseIdAndTimeEntity {

//    TODO: 채팅방 이름은 프론트에서 사용하지않지만 확장성을 대비하여 살려둠.
    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(max = 100, message = "채팅방 이름은 100자 이하여야 합니다")
    @Column(name = "name", nullable = false, length = 100)
    private String name; // 채팅방 이름

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>(); // 채팅 참여자 목록

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>(); // 채팅 메시지 목록
}
