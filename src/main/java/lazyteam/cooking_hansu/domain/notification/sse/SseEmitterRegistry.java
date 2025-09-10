package lazyteam.cooking_hansu.domain.notification.sse;

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
}
