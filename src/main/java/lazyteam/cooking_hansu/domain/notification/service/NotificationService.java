package lazyteam.cooking_hansu.domain.notification.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.notification.dto.NotificationDto;
import lazyteam.cooking_hansu.domain.notification.dto.NotificationListResponseDto;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationPublisher;
import lazyteam.cooking_hansu.domain.notification.repository.NotificationRepository;
import lazyteam.cooking_hansu.domain.notification.sse.SseEmitterRegistry;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lazyteam.cooking_hansu.domain.notification.dto.ChatNotificationDto;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    public UUID createAndDispatch(SseMessageDto sseMessageDto) {
        User recipient = userRepository.findById(sseMessageDto.getRecipientId())
                .orElseThrow(() -> new EntityNotFoundException("recipient"));

        Notification n = Notification.builder()
                .recipient(recipient)
                .content(sseMessageDto.getContent())
                .targetType(sseMessageDto.getTargetType())
                .targetId(sseMessageDto.getTargetId())
                .build();
        notificationRepository.save(n);

        notificationPublisher.publish(sseMessageDto);
        return n.getId();
    }

    @Transactional(readOnly = true)
    public NotificationListResponseDto getNotificationList(String cursor, int size) {
        UUID userId = AuthUtils.getCurrentUserId();
        List<Notification> notifications;
        
        if (cursor != null && !cursor.isEmpty()) {
            // cursor가 있으면 해당 cursor 이후부터 조회
            notifications = notificationRepository.findNextNotifications(
                userId, UUID.fromString(cursor), size + 1); // 다음 페이지 존재 여부 확인을 위해 +1
        } else {
            // cursor가 없으면 처음부터 조회
            notifications = notificationRepository.findFirstNotifications(
                userId, size + 1); // 다음 페이지 존재 여부 확인을 위해 +1
        }

        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size); // 마지막 하나는 제거
        }

        List<NotificationDto> notificationDtos = notifications.stream()
            .map(n -> NotificationDto.builder()
                .id(n.getId())
                .recipientId(n.getRecipient().getId())
                .content(n.getContent())
                .targetType(n.getTargetType())
                .targetId(n.getTargetId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        String nextCursor = hasNext ? notifications.get(notifications.size() - 1).getId().toString() : null;

        return NotificationListResponseDto.builder()
            .notifications(notificationDtos)
            .nextCursor(nextCursor)
            .hasNext(hasNext)
            .build();
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        UUID userId = AuthUtils.getCurrentUserId();
        return notificationRepository.countByRecipient_IdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    public void markRead(UUID notificationId) {
        UUID userId = AuthUtils.getCurrentUserId();
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 알림이 아닙니다.");
        }
        // 읽음 처리
        n.markRead();
    }

    public void markDeleted(UUID notificationId) {
        UUID userId = AuthUtils.getCurrentUserId();
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 알림이 아닙니다.");
        }

        n.markDeleted();
    }

     // SSE 연결 및 알림 구독
    public SseEmitter subscribeToNotifications() {
        UUID userId = AuthUtils.getCurrentUserId();
        return sseEmitterRegistry.connect(userId);
    }

     // 채팅방 온라인 참여 시 해당 채팅방의 알림들을 읽음 처리
    public void markChatNotificationsAsRead(UUID userId, Long chatRoomId) {
        // 채팅 관련 알림 중 해당 채팅방의 알림들을 읽음 처리
        List<Notification> chatNotifications = notificationRepository
            .findByRecipient_IdAndTargetTypeAndIsReadFalse(userId, lazyteam.cooking_hansu.domain.notification.entity.TargetType.CHAT);
        
        for (Notification notification : chatNotifications) {
            // targetId가 chatRoomId와 일치하는지 확인 (UUID를 String으로 변환하여 비교)
            if (notification.getTargetId().toString().equals(chatRoomId.toString())) {
                notification.markRead();
            }
        }
    }

     // 채팅 알림 생성 및 발송
    public void createAndDispatchChatNotification(ChatNotificationDto chatNotificationDto) {
        User recipient = userRepository.findById(chatNotificationDto.getRecipientId())
                .orElseThrow(() -> new EntityNotFoundException("recipient"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .content(chatNotificationDto.getContent())
                .targetType(chatNotificationDto.getTargetType())
                .targetId(chatNotificationDto.getTargetId())
                .roomId(chatNotificationDto.getChatRoomId())
                .build();
        
        notificationRepository.save(notification);
        notificationPublisher.publishChatNotification(chatNotificationDto);
    }
}
