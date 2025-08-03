package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
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
public class ChatMessage extends BaseIdAndTimeEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender; // 메시지 발신자 정보

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다")
    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText; // 메시지 내용

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType; // 메시지 타입 (예: TEXT, IMAGE, FILE 등)

    @Size(max = 512, message = "파일 URL은 512자 이하여야 합니다")
    @Column(name = "file_url")
    private String fileUrl; // 파일 URL (이미지, 파일 등)

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private String isDeleted = "N"; // 메시지 삭제 여부, 기본값은 "N" (삭제되지 않음)

    @OneToMany(mappedBy = "chatMessage", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReadStatus> readStatuses = new ArrayList<>(); // 읽음 상태
}
