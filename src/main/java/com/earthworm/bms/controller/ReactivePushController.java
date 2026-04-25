package com.earthworm.bms.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ReactivePushController {

    // Map of SessionID -> SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/api/reactive-updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(HttpSession session) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Keep open indefinitely
        String sessionId = session.getId();
        
        emitters.put(sessionId, emitter);

        emitter.onCompletion(() -> emitters.remove(sessionId));
        emitter.onTimeout(() -> emitters.remove(sessionId));
        emitter.onError((e) -> emitters.remove(sessionId));

        return emitter;
    }

    public void pushUpdate(String sessionId, Object updatePayload) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(updatePayload, MediaType.APPLICATION_JSON);
            } catch (Exception e) {
                emitters.remove(sessionId);
            }
        }
    }
}
