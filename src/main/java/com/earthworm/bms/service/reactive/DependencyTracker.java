package com.earthworm.bms.service.reactive;

import java.util.HashSet;
import java.util.Set;

/**
 * Uses ThreadLocal to track which Node IDs are accessed during a single evaluation.
 */
public class DependencyTracker {
    private static final ThreadLocal<Set<Long>> accessedNodes = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isTracking = ThreadLocal.withInitial(() -> false);

    public static void startTracking() {
        accessedNodes.set(new HashSet<>());
        isTracking.set(true);
    }

    public static void recordAccess(Long nodeId) {
        if (isTracking.get() && nodeId != null) {
            accessedNodes.get().add(nodeId);
        }
    }

    public static Set<Long> stopTracking() {
        Set<Long> nodes = accessedNodes.get();
        accessedNodes.remove();
        isTracking.set(false);
        return nodes;
    }
}
