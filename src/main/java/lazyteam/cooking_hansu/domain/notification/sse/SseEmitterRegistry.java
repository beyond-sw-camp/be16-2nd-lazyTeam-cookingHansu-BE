package lazyteam.cooking_hansu.domain.notification.sse;

import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseEmitterRegistry {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastHeartbeat = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userId) {
        // 기존 연결이 있으면 정리
        disconnect(userId);
        
        SseEmitter emitter = new SseEmitter(30L * 60 * 1000); // 30분으로 단축
        emitters.put(userId, emitter);
        lastHeartbeat.put(userId, System.currentTimeMillis());
        
        // 연결 성공 시 정리
        emitter.onTimeout(() -> {
            log.info("SSE timeout for user: {}", userId);
            disconnect(userId);
        });
        
        emitter.onCompletion(() -> {
            log.info("SSE completed for user: {}", userId);
            disconnect(userId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE error for user: {}", userId, ex);
            disconnect(userId);
        });
        
        try { 
            emitter.send(SseEmitter.event().name("connect").data("ok")); 
            log.info("SSE connected for user: {}", userId);
        } catch (IOException e) {
            log.error("Failed to send connect event to user: {}", userId, e);
            disconnect(userId);
        }
        
        return emitter;
    }

    public void send(UUID userId, Object payload) {
        SseEmitter e = emitters.get(userId);
        if (e == null) {
            return;
        }
        
        try { 
            e.send(SseEmitter.event().name("notify").data(payload)); 
            lastHeartbeat.put(userId, System.currentTimeMillis());
        }
        catch (IOException ex) { 
            log.error("Failed to send SSE message to user: {}", userId, ex);
            disconnect(userId);
        }
    }
    
    // 연결 해제
    public void disconnect(UUID userId) {
        SseEmitter emitter = emitters.remove(userId);
        lastHeartbeat.remove(userId);
        
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.warn("Error completing SSE emitter for user: {}", userId, e);
            }
        }
    }
    
    // 연결 상태 확인
    public boolean isConnected(UUID userId) {
        return emitters.containsKey(userId);
    }
    
    // 활성 연결 수
    public int getActiveConnectionCount() {
        return emitters.size();
    }
    
    // 하트비트 업데이트
    public void updateHeartbeat(UUID userId) {
        if (emitters.containsKey(userId)) {
            lastHeartbeat.put(userId, System.currentTimeMillis());
        }
    }
    
    // 비활성 연결 정리 (5분 이상 비활성)
    public void cleanupInactiveConnections() {
        long now = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000; // 5분
        
        emitters.entrySet().removeIf(entry -> {
            UUID userId = entry.getKey();
            Long lastBeat = lastHeartbeat.get(userId);
            
            if (lastBeat == null || (now - lastBeat) > timeout) {
                log.info("Cleaning up inactive SSE connection for user: {}", userId);
                try {
                    entry.getValue().complete();
                } catch (Exception e) {
                    log.warn("Error completing inactive SSE emitter for user: {}", userId, e);
                }
                lastHeartbeat.remove(userId);
                return true;
            }
            return false;
        });
    }
}
