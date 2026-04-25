package com.earthworm.bms.service;

import com.earthworm.bms.service.reactive.DependencyTracker;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@SessionScope
public class JsScopeService {

    private final GlobalJsScopeService globalJsScopeService;
    private final Map<String, Object> sessionBindings = new ConcurrentHashMap<>();

    public JsScopeService(GlobalJsScopeService globalJsScopeService) {
        this.globalJsScopeService = globalJsScopeService;
    }

    public void put(String name, Object value) {
        sessionBindings.put(name, value);
    }

    /**
     * Evaluates expressions and returns a result containing the values AND the collected dependencies.
     */
    public record EvaluationResult(Map<String, Object> values, Map<String, Set<Long>> dependencies) {}

    public EvaluationResult evaluateWithTracking(List<String> expressions) {
        Map<String, Object> values = new HashMap<>();
        Map<String, Set<Long>> dependencies = new HashMap<>();

        try (Context context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowAllAccess(true)
                .build()) {
            
            String startupScript = globalJsScopeService.getStartupScript();
            if (startupScript != null && !startupScript.isEmpty()) {
                context.eval("js", startupScript);
            }

            Value bindings = context.getBindings("js");
            for (Map.Entry<String, Object> entry : sessionBindings.entrySet()) {
                bindings.putMember(entry.getKey(), entry.getValue());
            }

            for (String expression : expressions) {
                // *** TRACKING START ***
                DependencyTracker.startTracking();
                try {
                    Value result = context.eval("js", expression);
                    values.put(expression, toJavaObject(result));
                    
                    // Store Node IDs touched by this specific expression

                } catch (Exception e) {
                    values.put(expression, "Error: " + e.getMessage());
                } finally {
                    // *** TRACKING STOP ***
                    dependencies.put(expression, DependencyTracker.stopTracking());
                }
            }
        }
        return new EvaluationResult(values, dependencies);
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
