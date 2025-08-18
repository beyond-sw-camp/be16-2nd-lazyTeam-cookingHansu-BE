package lazyteam.cooking_hansu.domain.notification.repository;

import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // 목록(삭제 안 된 것만), 최신순
    List<Notification> findByRecipient_IdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);


}
