package com.earthworm.bms.service;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
public class StartupScriptRunner implements CommandLineRunner {

    private final GlobalJsScopeService globalJsScopeService;
    private final SchemaService schemaService;
    private final UserService userService;

    public StartupScriptRunner(GlobalJsScopeService globalJsScopeService, SchemaService schemaService, UserService userService) {
        this.globalJsScopeService = globalJsScopeService;
        this.schemaService = schemaService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:scripts/*.js");
        
        if (resources.length == 0) {
            System.out.println("No startup scripts found in 'classpath:scripts/'.");
            return;
        }

        // Get the single global context
        Context globalContext = globalJsScopeService.getGlobalContext();

        // Expose services globally
        globalContext.getBindings("js").putMember("schema", schemaService);
        globalContext.getBindings("js").putMember("userService", userService);

        // Execute all scripts into the global context ONCE at startup
        for (Resource resource : resources) {
            try {
                String scriptName = resource.getFilename();
                System.out.println("Executing startup script: " + scriptName);
                String scriptContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

                Source source = Source.newBuilder("js", scriptContent, scriptName).build();

                // Synchronize on the context just during startup to be absolutely safe
                synchronized (globalContext) {
                    globalContext.eval(source);
                }

                System.out.println("Script '" + scriptName + "' executed successfully.");
            } catch (Exception e) {
                System.err.println("Error executing script '" + resource.getFilename() + "': " + e.getMessage());
            }
        }
    }
}
