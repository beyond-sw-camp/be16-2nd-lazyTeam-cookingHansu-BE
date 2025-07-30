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

    // 알림 발신자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;  // COMMENT, CHAT, APPROVAL

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

}
