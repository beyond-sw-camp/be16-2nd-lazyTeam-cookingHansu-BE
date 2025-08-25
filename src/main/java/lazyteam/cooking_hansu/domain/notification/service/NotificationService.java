package lazyteam.cooking_hansu.domain.notification.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.notification.pubsub.NotificationPublisher;
import lazyteam.cooking_hansu.domain.notification.repository.NotificationRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
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

        // 저장된 알림의 ID를 SseMessageDto에 설정
        sseMessageDto.setId(n.getId());
        notificationPublisher.publish(sseMessageDto);
        return n.getId();
    }

    @Transactional
    public void markRead(UUID notificationId, UUID userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 알림이 아닙니다.");
        }
        // 읽음 처리
        n.markRead();
    }

    @Transactional
    public void markDeleted(UUID notificationId, UUID userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));

        if (!n.getRecipient().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 알림이 아닙니다.");
        }

        n.markDeleted();
    }

}
