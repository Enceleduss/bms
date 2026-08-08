package com.earthworm.bms.service;

import com.earthworm.bms.service.reactive.DependencyTracker;
import jakarta.annotation.PostConstruct;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SessionScope
public class JsScopeService {

    private final GlobalJsScopeService globalJsScopeService;
    private final StateStoreService stateStoreService;
    
    private Value sessionChildScope;
    private Value evaluateInScopeFn;
    private Value pageObject;

    public JsScopeService(GlobalJsScopeService globalJsScopeService, StateStoreService stateStoreService) {
        this.globalJsScopeService = globalJsScopeService;
        this.stateStoreService = stateStoreService;
    }

    @PostConstruct
    public void init() {
        Context globalContext = globalJsScopeService.getGlobalContext();
        
        synchronized (globalContext) {
            sessionChildScope = globalContext.eval("js", "Object.create(globalThis)");
            
            pageObject = globalContext.eval("js", "({})");
            sessionChildScope.putMember("page", pageObject);
            
            sessionChildScope.putMember("store", stateStoreService);
            
            evaluateInScopeFn = globalContext.getBindings("js").getMember("evaluateInScope");
        }
    }

    public void put(String name, Object value) {
        if (pageObject != null) {
            pageObject.putMember(name, value);
        }
    }

    public record EvaluationResult(Map<String, Object> values, Map<String, Set<Long>> dependencies) {}

    /**
     * Evaluates a list of expressions, tracks dependencies, and returns results.
     * This is used for initial component rendering.
     */
    public EvaluationResult evaluateWithTracking(List<String> expressions) {
        Map<String, Object> values = new HashMap<>();
        Map<String, Set<Long>> dependencies = new HashMap<>();
        
        Context globalContext = globalJsScopeService.getGlobalContext();

        synchronized (globalContext) {
            for (String expression : expressions) {
                DependencyTracker.start();
                try {
                    Value result = evaluateInScopeFn.execute(sessionChildScope, expression);
                    values.put(expression, toJavaObject(result));
                    dependencies.put(expression, DependencyTracker.stop());
                } catch (Exception e) {
                    values.put(expression, "Error: " + e.getMessage());
                    DependencyTracker.stop();
                }
            }
        }
        
        return new EvaluationResult(values, dependencies);
    }

    /**
     * Evaluates a single expression without dependency tracking.
     * This is used by NodeChangeService to re-evaluate expressions for reactive updates.
     */
    public Object evaluateSingleExpression(String expression) {
        Context globalContext = globalJsScopeService.getGlobalContext();
        synchronized (globalContext) {
            try {
                Value result = evaluateInScopeFn.execute(sessionChildScope, expression);
                return toJavaObject(result);
            } catch (Exception e) {
                System.err.println("Error evaluating single expression '" + expression + "': " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }
    }

    private Object toJavaObject(Value value) {
        if (value == null || value.isNull()) return null;
        if (value.isHostObject()) return value.asHostObject();
        if (value.isString()) return value.asString();
        if (value.isBoolean()) return value.asBoolean();
        if (value.isNumber()) return value.as(Number.class);
        if (value.hasMembers()) {
            try { return value.as(Map.class); } catch (Exception e) { return value.toString(); }
        }
        return value.toString();
    }
}
