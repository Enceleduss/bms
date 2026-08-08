package com.earthworm.bms.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.springframework.stereotype.Service;

@Service
public class GlobalJsScopeService {

    // The single, permanent Global Context for application-wide logic
    private Context globalContext;

    @PostConstruct
    public void init() {
        this.globalContext = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowAllAccess(true)
                .build();

        // Define a helper function to evaluate expressions within a specific child scope
        // using JavaScript's "with" statement. This acts as our scope bridge.
        this.globalContext.eval("js", "function evaluateInScope(scope, expr) { with (scope) { return eval(expr); } }");
    }

    @PreDestroy
    public void cleanup() {
        if (this.globalContext != null) {
            this.globalContext.close();
        }
    }

    public Context getGlobalContext() {
        return globalContext;
    }
}
