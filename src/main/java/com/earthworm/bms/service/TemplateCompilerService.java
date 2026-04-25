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
import java.util.stream.Collectors;

@Service
public class TemplateCompilerService {

    public record CompilationResult(String js, Map<String, String> expressions) {}

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public CompilationResult compile(String xml, String functionName) throws IOException {
        Set<String> compiledComponents = new HashSet<>();
        StringBuilder fullJsOutput = new StringBuilder();
        Map<String, String> allExpressions = new HashMap<>();

        compileRecursive(xml, functionName, fullJsOutput, allExpressions, compiledComponents);

        return new CompilationResult(fullJsOutput.toString(), allExpressions);
    }

    private void compileRecursive(String xml, String functionName, StringBuilder fullJsOutput, 
                                  Map<String, String> allExpressions, Set<String> compiledComponents) throws IOException {
        
        if (compiledComponents.contains(functionName)) {
            return;
        }
        compiledComponents.add(functionName);

        Map<String, String> expressionMap = new HashMap<>();
        Map<String, String> reverseMap = new HashMap<>();
        AtomicInteger expressionCounter = new AtomicInteger(0);
        AtomicInteger varCounter = new AtomicInteger(0);
        Map<String, String> dtVariables = new HashMap<>();
        List<String> componentArgs = new ArrayList<>();

        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());

        // Pre-Pass: Process <dt:comp-arg> to determine function signature
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

        // First Pass: Process <dt:var> tags
        for (Element varTag : doc.select("dt|var")) {
            String varName = varTag.attr("name");
            String varExpression = varTag.text().trim();
            dtVariables.put(varName, varExpression);
            varTag.remove();
        }

        // Second Pass: Generate JS
        for (Node node : doc.childNodes()) {
            generateJsForNode(node, jsBuilder, "fragment", varCounter, expressionCounter, expressionMap, reverseMap, dtVariables, fullJsOutput, allExpressions, compiledComponents, componentArgs);
        }

        jsBuilder.append("    return fragment;\n");
        jsBuilder.append("}\n");

        fullJsOutput.append(jsBuilder);
        allExpressions.putAll(expressionMap);
    }

    private void generateJsForNode(Node node, StringBuilder jsBuilder, String parentVar, AtomicInteger varCounter, 
                                   AtomicInteger expressionCounter, Map<String, String> expressionMap, Map<String, String> reverseMap,
                                   Map<String, String> dtVariables, StringBuilder fullJsOutput, Map<String, String> allExpressions, 
                                   Set<String> compiledComponents, List<String> componentArgs) throws IOException {
        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName();

            // Handle <dt:make-comp>
            if (tagName.equals("dt:make-comp")) {
                String componentName = element.attr("class");
                String childFunctionName = "render" + toCamelCase(componentName);
                String argsAttr = element.attr("args"); // e.g., "[myVar1, myVar2]"
                
                // Parse arguments
                List<String> callArgs = new ArrayList<>();
                if (argsAttr != null && !argsAttr.isEmpty() && argsAttr.startsWith("[") && argsAttr.endsWith("]")) {
                    String content = argsAttr.substring(1, argsAttr.length() - 1);
                    if (!content.trim().isEmpty()) {
                        for (String argVarName : content.split(",")) {
                            argVarName = argVarName.trim();
                            // If the argument is a dt:var, we need to pass its evaluated value.
                            // But wait, dt:var defines an expression. The client receives the *result* of that expression.
                            // The result is stored in data['expr_X'].
                            // So we need to find the label for the expression associated with argVarName.
                            
                            String expression = dtVariables.get(argVarName);
                            if (expression != null) {
                                String label = getLabelForExpression(expression, expressionCounter, expressionMap, reverseMap);
                                callArgs.add("data['" + label + "']");
                            } else {
                                // It might be a raw expression or a literal? 
                                // For simplicity, let's assume it MUST be a dt:var name or a component argument name.
                                if (componentArgs.contains(argVarName)) {
                                    // It's an argument passed to THIS component, pass it through
                                    callArgs.add(argVarName);
                                } else {
                                    // Treat as raw expression/literal? Or fail?
                                    // Let's treat as raw expression for flexibility
                                    String label = getLabelForExpression(argVarName, expressionCounter, expressionMap, reverseMap);
                                    callArgs.add("data['" + label + "']");
                                }
                            }
                        }
                    }
                }

                String childXml = loadComponentSource(componentName);
                if (childXml != null) {
                    compileRecursive(childXml, childFunctionName, fullJsOutput, allExpressions, compiledComponents);
                    
                    // Generate call: childFunc(data, arg1, arg2)
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
                
                // Check if it's a component argument first
                if (componentArgs.contains(varName)) {
                    String varNodeName = "txt" + varCounter.incrementAndGet();
                    jsBuilder.append(String.format("    const %s = document.createTextNode(%s || '');\n", varNodeName, varName));
                    jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
                    return;
                }

                String expression = dtVariables.get(varName);
                if (expression != null) {
                    String label = getLabelForExpression(expression, expressionCounter, expressionMap, reverseMap);
                    String varNodeName = "txt" + varCounter.incrementAndGet();
                    jsBuilder.append(String.format("    const %s = document.createTextNode(data['%s'] || '');\n", varNodeName, label));
                    jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
                } else {
                     // Fallback: treat varName as a raw expression (e.g. global var)
                    String label = getLabelForExpression(varName, expressionCounter, expressionMap, reverseMap);
                    String varNodeName = "txt" + varCounter.incrementAndGet();
                    jsBuilder.append(String.format("    const %s = document.createTextNode(data['%s'] || '');\n", varNodeName, label));
                    jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varNodeName));
                }
                return;
            }

            // Handle <dt:if> tag
            if (tagName.equals("dt:if")) {
                String conditionAttr = element.attr("cond");
                String expression = dtVariables.getOrDefault(conditionAttr, conditionAttr);
                
                // If condition is a component argument, use it directly
                if (componentArgs.contains(conditionAttr)) {
                     jsBuilder.append(String.format("    if (%s) {\n", conditionAttr));
                } else {
                    String condLabel = getLabelForExpression(expression, expressionCounter, expressionMap, reverseMap);
                    jsBuilder.append(String.format("    if (data['%s']) {\n", condLabel));
                }
                
                Element thenBlock = element.selectFirst("dt|then");
                if (thenBlock != null) {
                    for (Node child : thenBlock.childNodes()) {
                        generateJsForNode(child, jsBuilder, parentVar, varCounter, expressionCounter, expressionMap, reverseMap, dtVariables, fullJsOutput, allExpressions, compiledComponents, componentArgs);
                    }
                }
                
                jsBuilder.append("    } else {\n");
                
                Element elseBlock = element.selectFirst("dt|else");
                if (elseBlock != null) {
                    for (Node child : elseBlock.childNodes()) {
                        generateJsForNode(child, jsBuilder, parentVar, varCounter, expressionCounter, expressionMap, reverseMap, dtVariables, fullJsOutput, allExpressions, compiledComponents, componentArgs);
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
                    String label = getLabelForExpression(expr, expressionCounter, expressionMap, reverseMap);
                    jsBuilder.append(String.format("    %s.setAttribute('%s', data['%s'] || '');\n", varName, attr.getKey(), label));
                } else {
                    jsBuilder.append(String.format("    %s.setAttribute('%s', '%s');\n", varName, attr.getKey(), attrValue));
                }
            });

            jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varName));

            for (Node childNode : element.childNodes()) {
                generateJsForNode(childNode, jsBuilder, varName, varCounter, expressionCounter, expressionMap, reverseMap, dtVariables, fullJsOutput, allExpressions, compiledComponents, componentArgs);
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
                String label = getLabelForExpression(expression, expressionCounter, expressionMap, reverseMap);
                String varName = "txt" + varCounter.incrementAndGet();
                jsBuilder.append(String.format("    const %s = document.createTextNode(data['%s'] || '');\n", varName, label));
                jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varName));
            } else {
                String varName = "txt" + varCounter.incrementAndGet();
                String escapedText = textContent.replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
                jsBuilder.append(String.format("    const %s = document.createTextNode('%s');\n", varName, escapedText));
                jsBuilder.append(String.format("    %s.appendChild(%s);\n", parentVar, varName));
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
