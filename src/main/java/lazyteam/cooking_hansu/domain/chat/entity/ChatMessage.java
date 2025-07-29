package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.User;
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
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender; // 메시지 발신자 정보

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content; // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType; // 메시지 타입 (예: TEXT, IMAGE, FILE 등)

    @Column(name = "file_url")
    private String fileUrl; // 파일 URL (이미지, 파일 등)

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private String isDeleted = "N"; // 메시지 삭제 여부, 기본값은 "N" (삭제되지 않음)




    @OneToMany(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReadStatus> readStatuses = new ArrayList<>(); // 읽음 상태
}
