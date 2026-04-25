package com.earthworm.bms.service.reactive;

import com.earthworm.bms.controller.ReactivePushController;
import com.earthworm.bms.service.JsScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NodeChangeService {

    @Autowired
    private ReactiveRegistry registry;

    @Autowired
    private ReactivePushController pushController;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Call this whenever a GraphNode is modified in the database.
     */
    public void notifyChange(Long nodeId) {
        Set<ReactiveRegistry.Subscription> subscribers = registry.getSubscribers(nodeId);
        
        if (subscribers.isEmpty()) return;

        // Group by session to re-evaluate efficiently
        Map<String, List<ReactiveRegistry.Subscription>> bySession = new HashMap<>();
        subscribers.forEach(s -> bySession.computeIfAbsent(s.sessionId(), k -> new ArrayList<>()).add(s));

        bySession.forEach((sessionId, subs) -> {
            // Get the user's specific JsScopeService (session-scoped)
            // Note: Retrieving a session-scoped bean outside a request context is tricky.
            // For now, we'll assume the evaluation happens in the background.
            
            // Collect all unique expressions that need re-evaluation for this session
            List<String> expressionsToRun = new ArrayList<>();
            // Map label back to original expression (we need this mapping stored somewhere)
            // For now, let's assume we can trigger a refresh signal to the client.
            
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("type", "REFRESH_REQUIRED");
            updatePayload.put("nodeId", nodeId);
            
            pushController.pushUpdate(sessionId, updatePayload);
        });
    }
}
