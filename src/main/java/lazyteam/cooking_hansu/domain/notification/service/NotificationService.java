package lazyteam.cooking_hansu.domain.notification.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.notification.dto.NotificationDto;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.Notification;
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

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    @Transactional
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
    public List<NotificationDto> getNotificationList() {
        UUID userId = AuthUtils.getCurrentUserId();
        List<Notification> notificationList = notificationRepository.findByRecipient_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId);


        return notificationList.stream()
                .map(n -> NotificationDto.builder()
                        .id(n.getId())
                        .recipientId(n.getRecipient().getId())
                        .content(n.getContent())
                        .targetType(n.getTargetType())
                        .targetId(n.getTargetId())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
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

    public SseEmitter subscribeToNotifications(UUID userId) {
        return sseEmitterRegistry.connect(userId);
    }
}
