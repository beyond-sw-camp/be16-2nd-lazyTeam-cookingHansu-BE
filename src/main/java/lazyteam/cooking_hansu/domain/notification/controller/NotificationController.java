package lazyteam.cooking_hansu.domain.notification.controller;

import lazyteam.cooking_hansu.domain.notification.dto.NotificationDto;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return notificationService.subscribeToNotifications();
    }

    // 목록 조회
    @GetMapping
    public ResponseEntity<?> list() {
        List<NotificationDto> notificationList = notificationService.getNotificationList();
        return ResponseEntity.ok(ResponseDto.ok(notificationList, HttpStatus.OK));
    }

    // 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> read(@PathVariable UUID id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(ResponseDto.ok(null, HttpStatus.OK));
    }
    
    // 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        notificationService.markDeleted(id);
        return ResponseEntity.ok(ResponseDto.ok(null, HttpStatus.OK));
    }
}
