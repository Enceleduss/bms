package com.earthworm.bms.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A global (singleton) service to store the mapping of React-assigned expression IDs
 * to their raw GraalVM JavaScript expression strings.
 * This ensures that if the same expression string is used across different sessions or components,
 * it maps to the same ID and is stored only once.
 */
@Service
public class GlobalExpressionStoreService {

    // Map: expressionId (from React) -> rawExpressionString
    private final Map<String, String> expressionMap = new ConcurrentHashMap<>();

    /**
     * Registers a batch of expressions from the client.
     * This method is idempotent: if an expressionId already exists, its value is updated.
     * @param clientExpressions A map of expressionId -> rawExpressionString.
     */
    public void registerExpressions(Map<String, String> clientExpressions) {
        expressionMap.putAll(clientExpressions);
    }

    /**
     * Retrieves the raw expression string for a given expression ID.
     * @param expressionId The ID assigned by the React client.
     * @return The raw GraalVM JavaScript expression string.
     */
    public String getRawExpressionString(String expressionId) {
        return expressionMap.get(expressionId);
    }

    /**
     * Retrieves a map of raw expression strings for a given list of expression IDs.
     * @param expressionIds A list of expression IDs.
     * @return A map of expressionId -> rawExpressionString.
     */
    public Map<String, String> getRawExpressionStrings(List<String> expressionIds) {
        Map<String, String> result = new HashMap<>();
        for (String id : expressionIds) {
            String expr = expressionMap.get(id);
            if (expr != null) {
                result.put(id, expr);
            }
        }
        return result;
    }

    /**
     * Unregisters expressions (for cleanup, if needed).
     */
    public void unregisterExpressions(List<String> expressionIds) {
        expressionIds.forEach(expressionMap::remove);
    }
}
