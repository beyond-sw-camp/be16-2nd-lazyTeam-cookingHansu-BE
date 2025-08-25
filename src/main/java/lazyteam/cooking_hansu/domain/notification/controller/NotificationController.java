package lazyteam.cooking_hansu.domain.notification.controller;

import lazyteam.cooking_hansu.domain.notification.dto.NotificationDto;
import lazyteam.cooking_hansu.domain.notification.entity.Notification;
import lazyteam.cooking_hansu.domain.notification.repository.NotificationRepository;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.notification.sse.SseEmitterRegistry;
import lazyteam.cooking_hansu.global.auth.AuthUtils;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final AuthUtils authUtils;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam UUID userId) {
        return sseEmitterRegistry.connect(userId);
    }

    // 목록 조회
    @GetMapping
    public ResponseEntity<?> list() {
        UUID userId = authUtils.getCurrentUserId();
        List<Notification> notificationList = notificationRepository.findByRecipient_IdAndIsDeletedFalseOrderByCreatedAtDesc(userId);

        List<NotificationDto> body = notificationList.stream()
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

        return ResponseEntity.ok(ResponseDto.ok(body, HttpStatus.OK));
    }

    // 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> read(@PathVariable UUID id) {
        System.out.println("알림 읽음 처리 요청 받음 ID: " + id);
        UUID userId = authUtils.getCurrentUserId();
        System.out.println("현재 사용자 ID: " + userId);
        notificationService.markRead(id, userId);
        return ResponseEntity.ok(ResponseDto.ok(null, HttpStatus.OK));
    }

    // 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        UUID userId = authUtils.getCurrentUserId();
        notificationService.markDeleted(id, userId);
        return ResponseEntity.ok(ResponseDto.ok(null, HttpStatus.OK));
    }
}
