package com.earthworm.bms.controller;

import com.earthworm.bms.service.JsScopeService;
import com.earthworm.bms.service.TemplateCompilerService;
import com.earthworm.bms.service.reactive.ReactiveRegistry;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class TemplateController {

    @Autowired
    private TemplateCompilerService compilerService;

    @Autowired
    private JsScopeService jsScopeService;

    @Autowired
    private ReactiveRegistry reactiveRegistry;

    private static final Path CACHE_DIR = Paths.get("target", "generated-js");
    private static final Map<String, Map<String, String>> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    @GetMapping(value = "/components/{componentName}.js", produces = "application/javascript")
    public ResponseEntity<byte[]> getCompiledComponent(@PathVariable String componentName) throws IOException {
        Files.createDirectories(CACHE_DIR);

        // Look for .dt files now
        Path sourcePath = new ClassPathResource("templates/components/" + componentName + ".dt").getFile().toPath();
        Path cachedJsPath = CACHE_DIR.resolve(componentName + ".js");

        long sourceLastModified = Files.getLastModifiedTime(sourcePath).toMillis();
        long cachedLastModified = Files.exists(cachedJsPath) ? Files.getLastModifiedTime(cachedJsPath).toMillis() : -1;

        if (sourceLastModified > cachedLastModified) {
            compileAndCache(componentName, sourcePath, cachedJsPath);
        } else if (!EXPRESSION_CACHE.containsKey(componentName)) {
            compileAndCache(componentName, sourcePath, cachedJsPath);
        }

        byte[] fileContent = Files.readAllBytes(cachedJsPath);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/javascript")).body(fileContent);
    }

    @GetMapping("/api/evaluate/{componentName}")
    @ResponseBody
    public Map<String, Object> evaluateExpressions(@PathVariable String componentName, HttpSession session) throws IOException {
        Map<String, String> labelToExpressionMap = EXPRESSION_CACHE.get(componentName);

        if (labelToExpressionMap == null) {
            Path sourcePath = new ClassPathResource("templates/components/" + componentName + ".dt").getFile().toPath();
            Path cachedJsPath = CACHE_DIR.resolve(componentName + ".js");
            Files.createDirectories(CACHE_DIR);
            compileAndCache(componentName, sourcePath, cachedJsPath);
            labelToExpressionMap = EXPRESSION_CACHE.get(componentName);
        }

        List<String> expressions = new ArrayList<>(labelToExpressionMap.values());
        
        // Evaluate with dependency tracking
        JsScopeService.EvaluationResult result = jsScopeService.evaluateWithTracking(expressions);

        // Register dependencies in the registry
        String sessionId = session.getId();
        Map<String, String> finalLabelToExpressionMap = labelToExpressionMap;
        result.dependencies().forEach((expression, nodeIds) -> {
            // Find the label for this expression
            String label = finalLabelToExpressionMap.entrySet().stream()
                    .filter(e -> e.getValue().equals(expression))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
            
            if (label != null) {
                nodeIds.forEach(nodeId -> reactiveRegistry.register(nodeId, sessionId, componentName, label));
            }
        });

        // Transform to Label -> Value map for client
        Map<String, Object> labelToValueMap = new HashMap<>();
        labelToExpressionMap.forEach((label, expression) -> {
            labelToValueMap.put(label, result.values().get(expression));
        });

        return labelToValueMap;
    }

    private void compileAndCache(String componentName, Path sourcePath, Path cachedJsPath) throws IOException {
        String sourceXml = Files.readString(sourcePath);
        String functionName = "render" + toCamelCase(componentName);
        TemplateCompilerService.CompilationResult result = compilerService.compile(sourceXml, functionName);
        Files.writeString(cachedJsPath, result.js());
        EXPRESSION_CACHE.put(componentName, result.expressions());
    }

    private String toCamelCase(String s) {
        String[] parts = s.split("-");
        StringBuilder camelCaseString = new StringBuilder(parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1));
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
        }
        return camelCaseString.toString();
    }
}
