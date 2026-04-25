package com.earthworm.bms.service.reactive;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReactiveRegistry {

    // Map of NodeID -> Set of Subscriptions
    private final Map<Long, Set<Subscription>> registry = new ConcurrentHashMap<>();

    public record Subscription(String sessionId, String componentName, String label) {}

    public void register(Long nodeId, String sessionId, String componentName, String label) {
        registry.computeIfAbsent(nodeId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(new Subscription(sessionId, componentName, label));
    }

    public Set<Subscription> getSubscribers(Long nodeId) {
        return registry.getOrDefault(nodeId, Collections.emptySet());
    }

    public void unregisterSession(String sessionId) {
        // Cleanup when user logs out or session expires
        registry.values().forEach(subs -> subs.removeIf(s -> s.sessionId().equals(sessionId)));
        registry.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
