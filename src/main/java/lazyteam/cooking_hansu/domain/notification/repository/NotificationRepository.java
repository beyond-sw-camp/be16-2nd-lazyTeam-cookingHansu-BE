package lazyteam.cooking_hansu.domain.notification.repository;

import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // 목록(삭제 안 된 것만), 최신순
    List<Notification> findByRecipient_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    // 채팅 알림 읽음 처리를 위한 메서드
    List<Notification> findByRecipient_IdAndTargetTypeAndIsReadFalse(UUID userId, TargetType targetType);
}
