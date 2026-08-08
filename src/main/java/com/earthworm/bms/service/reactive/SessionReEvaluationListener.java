package com.earthworm.bms.service.reactive;

import com.earthworm.bms.controller.ReactivePushController;
import com.earthworm.bms.service.GlobalExpressionStoreService;
import com.earthworm.bms.service.JsScopeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@SessionScope
public class SessionReEvaluationListener {

    @Autowired
    private HttpSession httpSession; // To get the current session ID

    @Autowired
    private JsScopeService jsScopeService; // This will be the session's own JsScopeService

    @Autowired
    private GlobalExpressionStoreService globalExpressionStoreService;

    @Autowired
    private ReactivePushController pushController;

    @EventListener
    public void handleExpressionReEvaluation(ExpressionReEvaluationEvent event) {
        // Ensure this event is for *this* session
        if (!event.getSessionId().equals(httpSession.getId())) {
            return; // Not for this session, ignore
        }

        // Get the raw expression strings for these IDs from the global store
        Map<String, String> idToRawExpressionMap = globalExpressionStoreService.getRawExpressionStrings(new ArrayList<>(event.getExpressionIdsToReEvaluate()));
        List<String> rawExpressions = new ArrayList<>(idToRawExpressionMap.values());

        if (rawExpressions.isEmpty()) {
            return;
        }

        // Re-evaluate these specific raw expressions using *this session's* JsScopeService
        JsScopeService.EvaluationResult reEvaluationResult = jsScopeService.evaluateWithTracking(rawExpressions);

        // Prepare granular updates: Map expressionId -> new_value
        Map<String, Object> updates = new HashMap<>();
        event.getExpressionIdsToReEvaluate().forEach(id -> {
            String rawExpr = globalExpressionStoreService.getRawExpressionString(id);
            if (rawExpr != null) {
                Object newValue = reEvaluationResult.values().get(rawExpr);
                updates.put(id, newValue);
            }
        });

        // Push granular updates via SSE
        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("type", "EXPRESSION_UPDATE");
        updatePayload.put("nodeId", event.getNodeId()); // The node that changed
        updatePayload.put("updates", updates); // Map of expressionId -> new_value
        
        pushController.pushUpdate(event.getSessionId(), updatePayload);
    }
}
