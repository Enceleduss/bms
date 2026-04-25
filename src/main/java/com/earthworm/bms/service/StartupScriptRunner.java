package com.earthworm.bms.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
public class StartupScriptRunner implements CommandLineRunner {

    private final GlobalJsScopeService globalJsScopeService;

    public StartupScriptRunner(GlobalJsScopeService globalJsScopeService) {
        this.globalJsScopeService = globalJsScopeService;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("scripts/startup_script.js");
        if (resource.exists()) {
            try {
                String scriptContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                globalJsScopeService.setStartupScript(scriptContent);
                System.out.println("Startup script 'startup_script.js' loaded into memory.");
            } catch (Exception e) {
                System.err.println("Error loading startup script 'startup_script.js': " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No startup script 'scripts/startup_script.js' found. Skipping.");
        }
    }
}
