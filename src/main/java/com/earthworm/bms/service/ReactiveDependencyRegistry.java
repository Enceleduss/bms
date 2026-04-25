package com.earthworm.bms.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReactiveDependencyRegistry {

    // Map: NodeId -> Set of Subscriptions
    // To scale, we only store entries for nodes being ACTIVELY viewed.
    private final Map<Long, Set<ExpressionSubscription>> nodeSubscriptions = new ConcurrentHashMap<>();

    public record ExpressionSubscription(String sessionId, String componentName, String expressionLabel) {}

    /**
     * Registers that a specific expression depends on a specific GraphNode.
     */
    public void registerDependency(Long nodeId, String sessionId, String componentName, String label) {
        nodeSubscriptions.computeIfAbsent(nodeId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(new ExpressionSubscription(sessionId, componentName, label));
    }

    /**
     * Returns all active expressions that need re-evaluation because this node changed.
     */
    public Set<ExpressionSubscription> getSubscribersForNode(Long nodeId) {
        return nodeSubscriptions.getOrDefault(nodeId, Collections.emptySet());
    }

    /**
     * Cleanup: When a session ends, we should ideally remove its subscriptions to save memory.
     */
    public void removeSubscriptionsForSession(String sessionId) {
        nodeSubscriptions.values().forEach(set -> set.removeIf(sub -> sub.sessionId().equals(sessionId)));
        // Optional: Remove empty sets from the map to keep it lean
        nodeSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
