package com.earthworm.bms.controller;

import com.earthworm.bms.service.GlobalExpressionStoreService;
import com.earthworm.bms.service.JsScopeService;
import com.earthworm.bms.service.reactive.ReactiveNodeRegistry;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class ExpressionController {

    @Autowired
    private JsScopeService jsScopeService;

    @Autowired
    private ReactiveNodeRegistry reactiveNodeRegistry;

    @Autowired
    private GlobalExpressionStoreService globalExpressionStoreService; // Use global store

    /**
     * Endpoint for React to register expression IDs and their raw strings.
     * This should be called once per unique expression ID/string combination.
     * @param expressionsMap A map of expressionId -> rawExpressionString.
     */
    @PostMapping("/api/register-expressions")
    public void registerExpressions(@RequestBody List<TheExpression> expressions) {
        Map<String, String> expressionsMap = expressions.stream().peek(System.out::println).collect(Collectors.toMap(TheExpression::id,TheExpression::expression));
        globalExpressionStoreService.registerExpressions(expressionsMap);
    }

    /**
     * Evaluates a list of GraalVM expressions (identified by their IDs) and registers their dependencies.
     * This endpoint is designed to be called by the React frontend.
     * @param expressionIds A list of React-assigned expression IDs to evaluate.
     * @param session The current HTTP session.
     * @return A map of expressionId -> evaluated value.
     */
    @PostMapping("/api/evaluate-expressions")
    public Map<String, Object> evaluateExpressions(@RequestBody List<String> expressionIds, HttpSession session) {
        // Retrieve raw expression strings from the global store
        Map<String, String> idToRawExpressionMap = globalExpressionStoreService.getRawExpressionStrings(expressionIds);
        List<String> rawExpressions = new java.util.ArrayList<>(idToRawExpressionMap.values());

        // Evaluate with dependency tracking
        JsScopeService.EvaluationResult result = jsScopeService.evaluateWithTracking(rawExpressions);

        // Register dependencies in the registry
        String sessionId = session.getId();
        String componentName = "react-frontend"; // A generic name for the React frontend

        result.dependencies().forEach((rawExpression, nodeIds) -> {
            // Find the expressionId corresponding to this rawExpression
            String expressionId = idToRawExpressionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(rawExpression))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null); // Should not be null if logic is correct

            if (expressionId != null) {
                nodeIds.forEach(nodeId -> reactiveNodeRegistry.register(nodeId, sessionId, componentName, expressionId));
            }
        });

        // Transform to ExpressionId -> Value map for client
        Map<String, Object> expressionIdToValueMap = new HashMap<>();
        idToRawExpressionMap.forEach((expressionId, rawExpression) -> {
            expressionIdToValueMap.put(expressionId, result.values().get(rawExpression));
        });

        return expressionIdToValueMap;
    }
}

record TheExpression(String id, String expression){};