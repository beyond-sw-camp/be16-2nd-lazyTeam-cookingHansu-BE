package lazyteam.cooking_hansu.domain.notification.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lazyteam.cooking_hansu.domain.notification.dto.NotificationListResponseDto;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpServletResponse response) {
        // 반드시 바디 쓰기 전에 설정
        response.setCharacterEncoding("UTF-8");
        response.setHeader("X-Accel-Buffering", "no");              // nginx 버퍼링 off
        response.setHeader("Cache-Control", "no-cache, no-transform"); // 캐시/변환 금지
        response.setHeader("Connection", "keep-alive");              // (HTTP/1.1일 때 유효)

        return notificationService.subscribeToNotifications();
    }

    // SSE 연결 해제
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        notificationService.disconnectCurrentUser();
        return ResponseEntity.ok(ResponseDto.ok("SSE 연결이 해제되었습니다.", HttpStatus.OK));
    }

    // 목록 조회 (Cursor pagination)
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "8") int size) {
        NotificationListResponseDto notificationResponse = notificationService.getNotificationList(cursor, size);
        return ResponseEntity.ok(ResponseDto.ok(notificationResponse, HttpStatus.OK));
    }

    // 안 읽음 개수 조회
    @GetMapping("/unread/count")
    public ResponseEntity<?> unreadCount() {
        Long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ResponseDto.ok(count, HttpStatus.OK));
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

