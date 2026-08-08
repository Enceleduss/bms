package com.earthworm.bms.service.reactive;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReactiveNodeRegistry {

    // Map of NodeID -> Set of Subscriptions
    private final Map<Long, Set<Subscription>> registry = new ConcurrentHashMap<>();

    // Subscription now holds the React-assigned expressionId
    public record Subscription(String sessionId, String componentName, String expressionId) {}

    public void register(Long nodeId, String sessionId, String componentName, String expressionId) {
        registry.computeIfAbsent(nodeId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(new Subscription(sessionId, componentName, expressionId));
    }

    public Set<Subscription> getSubscribers(Long nodeId) {
        return registry.getOrDefault(nodeId, Collections.emptySet());
    }

    public void unregisterSession(String sessionId) {
        registry.values().forEach(subs -> subs.removeIf(s -> s.sessionId().equals(sessionId)));
        registry.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    public void unregisterComponent(String sessionId, String componentName) {
        registry.values().forEach(subs -> subs.removeIf(s -> 
            s.sessionId().equals(sessionId) && s.componentName().equals(componentName)
        ));
        registry.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
