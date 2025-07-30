package lazyteam.cooking_hansu.domain.notification.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseIdAndTimeEntity {

    // 알림 수신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

//    // 알림 발신자
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "sender_id", nullable = false)
//    private User sender;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "sender_type", nullable = false)
//    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;  // 알림 대상 타입

    @Column(name = "target_id", nullable = false)
    private UUID targetId; // 알림 대상 ID (예: 게시글 ID, 댓글 ID 등)

    @Column(name = "target_title", length = 255)
    private String targetTitle;  // 대상 제목 (예: 게시글 제목, 강의 제목)

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    // 비즈니스 메서드

    // 알림 읽음 처리
    public void markAsRead() {
        this.isRead = true;
    }

    // 알림 삭제 처리
    public void markAsDeleted() {
        this.isDeleted = true;
    }

    // 알림 복원 처리
    public void restore() {
        this.isDeleted = false;
    }

    // 알림이 읽히지 않았는지 확인
    public boolean isUnread() {
        return !this.isRead;
    }

    // 알림이 삭제되지 않았는지 확인
    public boolean isActive() {
        return !this.isDeleted;
    }

    // 알림 내용 업데이트
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
    }

    // 대상 제목 업데이트
    public void updateTargetTitle(String targetTitle) {
        if (targetTitle != null && !targetTitle.trim().isEmpty()) {
            this.targetTitle = targetTitle.trim();
        }
    }
}
