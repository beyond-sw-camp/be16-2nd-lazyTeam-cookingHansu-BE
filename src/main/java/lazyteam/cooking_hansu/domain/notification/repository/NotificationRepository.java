package lazyteam.cooking_hansu.domain.notification.repository;

import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {


    // 목록(삭제 안 된 것만), 최신순 (커서 페이지네이션용) - 처음 조회
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findFirstNotifications(@Param("userId") UUID userId, @Param("size") int size);

    // 목록(삭제 안 된 것만), 안읽은 알림 우선, 최신순 (커서 페이지네이션용) - cursor 이후 조회
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isDeleted = false AND n.createdAt < :cursorTime ORDER BY n.isRead ASC, n.createdAt DESC")
    List<Notification> findNextNotifications(@Param("userId") UUID userId, @Param("cursorTime") LocalDateTime cursorTime, @Param("size") int size);

    // 안읽은 개수 조회
    Long countByRecipient_IdAndIsReadFalseAndIsDeletedFalse(UUID userId);

    // 채팅 알림 읽음 처리를 위한 메서드
    List<Notification> findByRecipient_IdAndTargetTypeAndIsReadFalse(UUID userId, TargetType targetType);

    // 모든 안읽은 알림 조회 (모두 읽음 처리용)
    List<Notification> findByRecipient_IdAndIsReadFalseAndIsDeletedFalse(UUID userId);
}
