package com.earthworm.bms.service;

import com.earthworm.bms.service.reactive.DependencyTracker;
import com.earthworm.bms.service.reactive.NodeChangeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A session-scoped service that acts as a server-side state store.
 * It holds the UI state in memory independently for each logged-in user.
 */
@Service
@SessionScope
public class StateStoreService {

    @Autowired
    private NodeChangeService nodeChangeService;

    @Autowired
    private HttpSession session;

    // The state map, unique to this user's session
    private final Map<String, Object> state = new ConcurrentHashMap<>();

    /**
     * Generates a unique virtual Node ID for a state key specific to this session.
     * This prevents a state change in User A's session from triggering a refresh in User B's session.
     */
    private long getVirtualNodeId(String key) {
        String uniqueIdentifier = session.getId() + ":" + key;
        return uniqueIdentifier.hashCode();
    }

    /**
     * Retrieves a value from the state store.
     * This method is designed to be called from GraalVM JavaScript.
     */
    public Object getState(String key) {
        long virtualNodeId = getVirtualNodeId(key);
        DependencyTracker.start(); // Ensure tracking is active
        DependencyTracker.recordAccess(virtualNodeId);
        return state.get(key);
    }

    /**
     * Updates a value in the state store and notifies the reactive system.
     * This method is designed to be called from GraalVM JavaScript.
     */
    public void setState(String key, Object value) {
        state.put(key, value);
        
        long virtualNodeId = getVirtualNodeId(key);
        nodeChangeService.notifyChange(virtualNodeId);
    }

    /**
     * Initializes the store with some default values.
     */
    public void initializeDefaultState() {
        state.put("theme", "light");
        state.put("notificationCount", 0);
    }
}
