package lazyteam.cooking_hansu.domain.notification.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(UUID userId) {
        SseEmitter emitter = new SseEmitter(60L * 60 * 1000);
        emitters.put(userId, emitter);
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onCompletion(() -> emitters.remove(userId));
        
        try { emitter.send(SseEmitter.event().name("connect").data("ok")); } catch (IOException ignored) {}
        return emitter;
    }

    public void send(UUID userId, Object payload) {
        SseEmitter e = emitters.get(userId);
        if (e == null) {
            return;
        }
        
        try { 
            e.send(SseEmitter.event().name("notify").data(payload)); 
        }
        catch (IOException ex) { 
            emitters.remove(userId); 
        }
    }
}
