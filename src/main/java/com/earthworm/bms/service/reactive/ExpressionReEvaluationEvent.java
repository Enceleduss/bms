package com.earthworm.bms.service.reactive;

import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Set;

public class ExpressionReEvaluationEvent extends ApplicationEvent {
    private final String sessionId;
    private final Long nodeId;
    private final Set<String> expressionIdsToReEvaluate;

    public ExpressionReEvaluationEvent(Object source, String sessionId, Long nodeId, Set<String> expressionIdsToReEvaluate) {
        super(source);
        this.sessionId = sessionId;
        this.nodeId = nodeId;
        this.expressionIdsToReEvaluate = expressionIdsToReEvaluate;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public Set<String> getExpressionIdsToReEvaluate() {
        return expressionIdsToReEvaluate;
    }
}
