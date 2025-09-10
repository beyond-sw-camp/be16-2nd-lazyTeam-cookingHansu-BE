package lazyteam.cooking_hansu.domain.chat.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 ID

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 메시지 생성 시간

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 메시지 수정 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom; // 채팅방 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender; // 메시지 발신자 정보


    @Size(max = 1000, message = "메시지 내용은 1000자 이하여야 합니다")
    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText; // 메시지 내용


    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private String isDeleted = "N"; // 메시지 삭제 여부, 기본값은 "N" (삭제되지 않음)

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatFile> files = new ArrayList<>();
}
