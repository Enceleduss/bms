package com.earthworm.bms.service.reactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeChangeService {

    @Autowired
    private ReactiveNodeRegistry registry;

    @Autowired
    private ApplicationEventPublisher eventPublisher; // To publish events

    /**
     * Call this whenever a GraphNode is modified in the database.
     * This will trigger publishing an event for each affected session.
     */
    public void notifyChange(Long nodeId) {
        Set<ReactiveNodeRegistry.Subscription> subscribers = registry.getSubscribers(nodeId);
        
        if (subscribers.isEmpty()) return;

        // Group by session to publish one event per session
        Map<String, Set<String>> expressionIdsBySession = new HashMap<>();
        subscribers.forEach(s -> 
            expressionIdsBySession.computeIfAbsent(s.sessionId(), k -> new HashSet<>()).add(s.expressionId())
        );

        expressionIdsBySession.forEach((sessionId, expressionIdsToReEvaluate) -> {
            // Publish an event for each affected session
            eventPublisher.publishEvent(new ExpressionReEvaluationEvent(this, sessionId, nodeId, expressionIdsToReEvaluate));
        });
    }
}
