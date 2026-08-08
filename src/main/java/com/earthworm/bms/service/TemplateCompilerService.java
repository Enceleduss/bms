package com.earthworm.bms.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateCompilerService {

    public record CompilationResult(String js, Map<String, String> expressions) {}

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public CompilationResult compile(String xml, String functionName) throws IOException {
        Set<String> compiledComponents = new HashSet<>();
        StringBuilder fullJsOutput = new StringBuilder();
        
        // THESE MUST BE GLOBAL FOR THE ENTIRE COMPILATION BUNDLE
        Map<String, String> globalExpressionMap = new HashMap<>(); // For data['expr_N'] -> raw expression
        Map<String, String> globalReverseMap = new HashMap<>(); // For raw expression -> data['expr_N']
        AtomicInteger globalExpressionCounter = new AtomicInteger(0);

        compileRecursive(xml, functionName, fullJsOutput, globalExpressionMap, globalReverseMap, globalExpressionCounter, compiledComponents);

        return new CompilationResult(fullJsOutput.toString(), globalExpressionMap);
    }

    private void compileRecursive(String xml, String functionName, StringBuilder fullJsOutput, 
                                  Map<String, String> allExpressions, Map<String, String> globalReverseMap, 
                                  AtomicInteger globalExpressionCounter, Set<String> compiledComponents) throws IOException {
        
        if (compiledComponents.contains(functionName)) {
            return;
        }
        compiledComponents.add(functionName);

        // This is local to each component's generated function
        AtomicInteger varCounter = new AtomicInteger(0);
        
        // Stores dt:var name -> expressionString (e.g., "fullName" -> "user.name.toUpperCase()")
        Map<String, String> dtVarNameToExpression = new HashMap<>();
        // Stores dt:var name -> label (e.g., "fullName" -> "expr_0")
        Map<String, String> dtVarNameToLabel = new HashMap<>();

        List<String> componentArgs = new ArrayList<>(); // For dt:comp-arg

        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        // Pre-Pass 1: Process <dt:comp-arg> to determine function signature
        for (Element argTag : doc.select("dt|comp-arg")) {
            String argName = argTag.attr("name");
            componentArgs.add(argName);
            argTag.remove();
        }

        // Build function signature: function renderName(data, arg1, arg2)
        StringBuilder jsBuilder = new StringBuilder();
        jsBuilder.append(String.format("function %s(data", functionName));
        for (String arg : componentArgs) {
            jsBuilder.append(", ").append(arg);
        }
        jsBuilder.append(") {\n");
        jsBuilder.append("    const fragment = document.createDocumentFragment();\n");

        // Pre-Pass 2: Process <dt:hostPageVar> tags to set values in the server-side 'env' object
        for (Element varTag : doc.select("dt|hostPageVar")) {
            String varName = varTag.attr("name");
            String varExpression = varTag.text().trim();
            
            // We need to tell the server to evaluate this expression and assign it to env[varName].
            // To do this, we create a special expression that will be executed on the server.
            // Notice we use the expression directly, not the evaluated result.
            String assignmentExpression = String.format("env[%s] = %s", varName, varExpression);
            
            // Get a label for this assignment expression so it gets sent to the server for evaluation.
            // The server will execute it and return the result (which we ignore on the client).
            getLabelForExpression(assignmentExpression, globalExpressionCounter, allExpressions, globalReverseMap);
            
            varTag.remove(); // Remove the tag from the DOM
        }

        // Pre-Pass 3: Process <dt:var> tags to declare local variables
        for (Element varTag : doc.select("dt|var")) {
            String varName = varTag.attr("name");
            String varExpression = varTag.text().trim();
            
            // Get a globally unique label for this expression
            String label = getLabelForExpression(varExpression, globalExpressionCounter, allExpressions, globalReverseMap);
            
            dtVarNameToExpression.put(varName, varExpression); // Store original expression
            dtVarNameToLabel.put(varName, label); // Store label for lookup
            
            // Generate local JS variable declaration: const fullName = data['expr_0'];
            jsBuilder.append(String.format("    const %s = data['%s'];\n", varName, label));
            
            varTag.remove(); // Remove the tag from the DOM
        }

        // Second Pass: Generate JS from the remaining DOM
        for (Node node : doc.childNodes()) {
            generateJsForNode(node, jsBuilder, "fragment", varCounter, globalExpressionCounter, allExpressions, globalReverseMap, dtVarNameToExpression, dtVarNameToLabel, fullJsOutput, compiledComponents, componentArgs);
        }

        jsBuilder.append("    return fragment;\n");
        jsBuilder.append("}\n");

        fullJsOutput.append(jsBuilder);
    }

    private void generateJsForNode(Node node, StringBuilder jsBuilder, String parentVar, AtomicInteger varCounter, 
                                   AtomicInteger expressionCounter, Map<String, String> expressionMap, Map<String, String> reverseMap,
                                   Map<String, String> dtVarNameToExpression, Map<String, String> dtVarNameToLabel,
                                   StringBuilder fullJsOutput, 
                                   Set<String> compiledComponents, List<String> componentArgs) throws IOException {
        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();

            // Handle <dt:make-comp>
            if (tagName.equals("dt:make-comp")) {
                String componentName = element.attr("class");
                String childFunctionName = "render" + toCamelCase(componentName);
                String argsAttr = element.attr("args");
                
                List<String> callArgs = new ArrayList<>();
                if (argsAttr != null && !argsAttr.isEmpty() && argsAttr.startsWith("[") && argsAttr.endsWith("]")) {
                    String content = argsAttr.substring(1, argsAttr.length() - 1);
                    if (!content.trim().isEmpty()) {
                        for (String argVarName : content.split(",")) {
                            argVarName = argVarName.trim();
                            // If argVarName is a dt:var, use its local JS variable name
                            if (dtVarNameToExpression.containsKey(argVarName)) {
                                callArgs.add(argVarName);
                            } 
                            // If argVarName is a component argument, use its local JS variable name
                            else if (componentArgs.contains(argVarName)) {
                                callArgs.add(argVarName);
                            }
                            // Otherwise, treat as a raw expression and look up its value from 'data'
                            else {
                                String label = getLabelForExpression(argVarName, expressionCounter, expressionMap, reverseMap);
                                callArgs.add("data['" + label + "']");
                            }
                        }
                    }
                }

                String childXml = loadComponentSource(componentName);
                if (childXml != null) {
                    compileRecursive(childXml, childFunctionName, fullJsOutput, expressionMap, reverseMap, expressionCounter, compiledComponents);
                    
                    StringBuilder callBuilder = new StringBuilder();
                    callBuilder.append(childFunctionName).append("(data");
                    for (String arg : callArgs) {
                        callBuilder.append(", ").append(arg);
                    }
                    callBuilder.append(")");
                    
                    jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, callBuilder.toString()));
                }
                return;
            }

            // Handle <dt:v> tag
            if (tagName.equals("dt:v")) {
                String varName = element.text().trim();
                String jsValueReference = getJsValueReference(varName, dtVarNameToExpression, dtVarNameToLabel, componentArgs, expressionCounter, expressionMap, reverseMap);
                
                String varNodeName = "txt" + varCounter.incrementAndGet();
                jsBuilder.append(String.format("    const %s = document.createTextNode(%s || '');\n", varNodeName, jsValueReference));
                jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
                return;
            }

            // Handle <dt:if> tag
            if (tagName.equals("dt:if")) {
                String conditionAttr = element.attr("cond");
                String jsConditionReference = getJsValueReference(conditionAttr, dtVarNameToExpression, dtVarNameToLabel, componentArgs, expressionCounter, expressionMap, reverseMap);
                
                jsBuilder.append(String.format("    if (%s) {\n", jsConditionReference));
                
                Element thenBlock = element.selectFirst("dt|then");
                if (thenBlock != null) {
                    for (Node child : thenBlock.childNodes()) {
                        generateJsForNode(child, jsBuilder, parentVar, varCounter, expressionCounter, expressionMap, reverseMap, dtVarNameToExpression, dtVarNameToLabel, fullJsOutput, compiledComponents, componentArgs);
                    }
                }
                
                jsBuilder.append("    } else {\n");
                
                Element elseBlock = element.selectFirst("dt|else");
                if (elseBlock != null) {
                    for (Node child : elseBlock.childNodes()) {
                        generateJsForNode(child, jsBuilder, parentVar, varCounter, expressionCounter, expressionMap, reverseMap, dtVarNameToExpression, dtVarNameToLabel, fullJsOutput, compiledComponents, componentArgs);
                    }
                }
                
                jsBuilder.append("    }\n");
                return;
            }
            
            if (tagName.equals("dt:then") || tagName.equals("dt:else")) {
                return;
            }

            // Standard HTML Element
            String varName = "el" + varCounter.incrementAndGet();
            jsBuilder.append(String.format("    const %s = document.createElement('%s');\n", varName, tagName));

            element.attributes().forEach(attr -> {
                String attrValue = attr.getValue();
                Matcher matcher = EXPRESSION_PATTERN.matcher(attrValue);
                if (matcher.find()) {
                    String expr = matcher.group(1);
                    String jsValueReference = getJsValueReference(expr, dtVarNameToExpression, dtVarNameToLabel, componentArgs, expressionCounter, expressionMap, reverseMap);
                    jsBuilder.append(String.format("    %s.setAttribute('%s', %s || '');\n", varName, attr.getKey(), jsValueReference));
                } else {
                    jsBuilder.append(String.format("    %s.setAttribute('%s', '%s');\n", varName, attr.getKey(), attrValue));
                }
            });

            jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varName));

            for (Node childNode : element.childNodes()) {
                generateJsForNode(childNode, jsBuilder, varName, varCounter, expressionCounter, expressionMap, reverseMap, dtVarNameToExpression, dtVarNameToLabel, fullJsOutput, compiledComponents, componentArgs);
            }

        } else if (node instanceof TextNode) {
            TextNode textNode = (TextNode) node;
            String textContent = textNode.getWholeText();
            
            if (textContent.trim().isEmpty()) {
                return;
            }

            Matcher matcher = EXPRESSION_PATTERN.matcher(textContent);
            if (matcher.find()) {
                String expression = matcher.group(1);
                String jsValueReference = getJsValueReference(expression, dtVarNameToExpression, dtVarNameToLabel, componentArgs, expressionCounter, expressionMap, reverseMap);
                String varNodeName = "txt" + varCounter.incrementAndGet();
                jsBuilder.append(String.format("    const %s = document.createTextNode(%s || '');\n", varNodeName, jsValueReference));
                jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
            } else {
                String varNodeName = "txt" + varCounter.incrementAndGet();
                String escapedText = textContent.replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
                jsBuilder.append(String.format("    const %s = document.createTextNode('%s');\n", varNodeName, escapedText));
                jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
            }
        }
    }

    private String getLabelForExpression(String expression, AtomicInteger expressionCounter, Map<String, String> expressionMap, Map<String, String> reverseMap) {
        if (reverseMap.containsKey(expression)) {
            return reverseMap.get(expression);
        } else {
            String label = "expr_" + expressionCounter.getAndIncrement();
            expressionMap.put(label, expression);
            reverseMap.put(expression, label);
            return label;
        }
    }

    // Helper to determine the correct JS reference (local var, component arg, or data['label'])
    private String getJsValueReference(String nameOrExpression, 
                                       Map<String, String> dtVarNameToExpression, Map<String, String> dtVarNameToLabel,
                                       List<String> componentArgs,
                                       AtomicInteger expressionCounter, Map<String, String> expressionMap, Map<String, String> reverseMap) {
        // 1. Is it a local dt:var?
        String splitted = nameOrExpression.split("\\.")[0];
        if (dtVarNameToLabel.containsKey(splitted)) {
            return nameOrExpression; // Use the local JS variable name directly
        }
        // 2. Is it a component argument?
        if (componentArgs.contains(splitted)) {
            return nameOrExpression; // Use the argument name directly
        }
        // 3. Otherwise, it's a raw expression, so get its label and reference it from the 'data' object
        String label = getLabelForExpression(nameOrExpression, expressionCounter, expressionMap, reverseMap);
        return "data['" + label + "']";
    }

    private String loadComponentSource(String componentName) {
        try {
            ClassPathResource compResource = new ClassPathResource("templates/components/" + componentName + ".comp");
            if (compResource.exists()) {
                return Files.readString(compResource.getFile().toPath());
            }
            ClassPathResource dtResource = new ClassPathResource("templates/components/" + componentName + ".dt");
            if (dtResource.exists()) {
                return Files.readString(dtResource.getFile().toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
