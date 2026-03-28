package com.schemascope.service;

import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaDependencyEdge;
import com.schemascope.domain.JavaDependencyGraph;
import com.schemascope.domain.JavaProjectScanResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaDependencyGraphBuilder {

    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("^import\\s+([a-zA-Z0-9_\\.]+);$");

    private static final Pattern FIELD_PATTERN =
            Pattern.compile("^(?:private|protected|public)?\\s*(?:static\\s+)?(?:final\\s+)?([A-Z][A-Za-z0-9_]*)\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*;$");

    private static final Pattern CONSTRUCTOR_PATTERN =
            Pattern.compile("^(?:public|protected|private)?\\s*([A-Z][A-Za-z0-9_]*)\\s*\\(([^)]*)\\)\\s*\\{$");

    private static final Pattern METHOD_PATTERN =
            Pattern.compile("^(?:public|protected|private|static|final|default|synchronized|abstract|native|strictfp|\\s)*" +
                    "[A-Za-z0-9_<>\\[\\],? ]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\(([^)]*)\\)\\s*\\{$");

    private static final Pattern LOCAL_VARIABLE_PATTERN =
            Pattern.compile("^(?:final\\s+)?([A-Z][A-Za-z0-9_]*)\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*=.*;$");

    private static final Pattern METHOD_CALL_PATTERN =
            Pattern.compile("(?:this\\.)?([a-zA-Z_][A-Za-z0-9_]*)\\s*\\.\\s*([a-zA-Z_][A-Za-z0-9_]*)\\s*\\(");

    public JavaDependencyGraph build(JavaProjectScanResult scanResult) throws IOException {
        List<JavaDependencyEdge> edges = new ArrayList<>();

        if (scanResult == null || scanResult.getComponents() == null) {
            return new JavaDependencyGraph(edges);
        }

        Map<String, JavaComponent> bySimpleName = new LinkedHashMap<>();
        for (JavaComponent component : scanResult.getComponents()) {
            if (component.getClassName() != null) {
                bySimpleName.put(component.getClassName(), component);
            }
        }

        LinkedHashSet<String> seen = new LinkedHashSet<>();

        for (JavaComponent owner : scanResult.getComponents()) {
            if (owner.getFilePath() == null) {
                continue;
            }

            Path filePath = Path.of(owner.getFilePath());
            if (!Files.exists(filePath)) {
                continue;
            }

            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            Map<String, String> importedSimpleNames = parseImports(lines);
            Map<String, String> fieldTypeByVariable = new LinkedHashMap<>();

            String currentMethodName = null;
            int braceDepth = 0;
            int methodBraceDepth = -1;
            Map<String, String> methodVariableTypes = new LinkedHashMap<>();

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                    continue;
                }

                String fieldType = parseFieldType(line);
                if (fieldType != null) {
                    String variableName = parseFieldVariableName(line);
                    String resolved = resolveComponentType(fieldType, importedSimpleNames, bySimpleName);
                    if (resolved != null && !resolved.equals(owner.getClassName())) {
                        addEdge(edges, seen, resolved, owner.getClassName(),
                                "FIELD_INJECTION", line, null, null);
                        if (variableName != null) {
                            fieldTypeByVariable.put(variableName, resolved);
                        }
                    }

                    braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                    continue;
                }

                ConstructorMatch constructorMatch = parseConstructor(line, owner.getClassName());
                if (constructorMatch != null) {
                    currentMethodName = owner.getClassName();
                    methodVariableTypes = new LinkedHashMap<>(constructorMatch.parameterTypeByVariable);

                    int depthAfterLine = braceDepth + countChar(rawLine, '{') - countChar(rawLine, '}');
                    methodBraceDepth = depthAfterLine;

                    for (Map.Entry<String, String> entry : constructorMatch.parameterTypeByVariable.entrySet()) {
                        String resolved = resolveComponentType(entry.getValue(), importedSimpleNames, bySimpleName);
                        if (resolved != null && !resolved.equals(owner.getClassName())) {
                            addEdge(edges, seen, resolved, owner.getClassName(),
                                    "CONSTRUCTOR_INJECTION", line, currentMethodName, null);
                            methodVariableTypes.put(entry.getKey(), resolved);
                        }
                    }

                    braceDepth = depthAfterLine;
                    continue;
                }

                MethodMatch methodMatch = parseMethod(line, owner.getClassName());
                if (methodMatch != null) {
                    currentMethodName = methodMatch.methodName;
                    methodVariableTypes = new LinkedHashMap<>(methodMatch.parameterTypeByVariable);

                    int depthAfterLine = braceDepth + countChar(rawLine, '{') - countChar(rawLine, '}');
                    methodBraceDepth = depthAfterLine;
                    braceDepth = depthAfterLine;
                    continue;
                }

                if (currentMethodName != null) {
                    LocalVariableMatch localVariableMatch = parseLocalVariable(line);
                    if (localVariableMatch != null) {
                        String resolved = resolveComponentType(localVariableMatch.typeName, importedSimpleNames, bySimpleName);
                        if (resolved != null) {
                            methodVariableTypes.put(localVariableMatch.variableName, resolved);
                        }
                    }

                    for (MethodCallMatch methodCall : parseMethodCalls(line)) {
                        String dependencyClassName = resolveReceiverType(methodCall.receiverVariableName,
                                fieldTypeByVariable, methodVariableTypes);

                        if (dependencyClassName == null || dependencyClassName.equals(owner.getClassName())) {
                            continue;
                        }

                        addEdge(edges, seen, dependencyClassName, owner.getClassName(),
                                "METHOD_CALL",
                                line,
                                currentMethodName,
                                methodCall.calledMethodName);
                    }
                }

                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');

                if (methodBraceDepth > 0 && braceDepth < methodBraceDepth) {
                    currentMethodName = null;
                    methodBraceDepth = -1;
                    methodVariableTypes.clear();
                }
            }
        }

        return new JavaDependencyGraph(edges);
    }

    private Map<String, String> parseImports(List<String> lines) {
        Map<String, String> imports = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            String fqcn = matcher.group(1);
            int idx = fqcn.lastIndexOf('.');
            if (idx > 0 && idx < fqcn.length() - 1) {
                imports.put(fqcn.substring(idx + 1), fqcn);
            }
        }

        return imports;
    }

    private String parseFieldType(String line) {
        Matcher matcher = FIELD_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private String parseFieldVariableName(String line) {
        Matcher matcher = FIELD_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    private ConstructorMatch parseConstructor(String line, String ownerClassName) {
        Matcher matcher = CONSTRUCTOR_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        String constructorName = matcher.group(1);
        if (!constructorName.equals(ownerClassName)) {
            return null;
        }

        return new ConstructorMatch(parseParameters(matcher.group(2)));
    }

    private MethodMatch parseMethod(String line, String ownerClassName) {
        Matcher matcher = METHOD_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        String methodName = matcher.group(1);
        if (methodName.equals(ownerClassName)) {
            return null;
        }

        return new MethodMatch(methodName, parseParameters(matcher.group(2)));
    }

    private Map<String, String> parseParameters(String parameterBlock) {
        Map<String, String> result = new LinkedHashMap<>();

        if (parameterBlock == null || parameterBlock.isBlank()) {
            return result;
        }

        String[] params = parameterBlock.split(",");
        for (String param : params) {
            String normalized = param.trim().replaceAll("\\s+", " ");
            String[] tokens = normalized.split(" ");
            if (tokens.length >= 2) {
                String variableName = tokens[tokens.length - 1];
                String typeName = tokens[tokens.length - 2];
                result.put(variableName, typeName);
            }
        }

        return result;
    }

    private LocalVariableMatch parseLocalVariable(String line) {
        Matcher matcher = LOCAL_VARIABLE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }

        return new LocalVariableMatch(matcher.group(1), matcher.group(2));
    }

    private List<MethodCallMatch> parseMethodCalls(String line) {
        List<MethodCallMatch> matches = new ArrayList<>();
        Matcher matcher = METHOD_CALL_PATTERN.matcher(line);

        while (matcher.find()) {
            matches.add(new MethodCallMatch(
                    matcher.group(1),
                    matcher.group(2)
            ));
        }

        return matches;
    }

    private String resolveReceiverType(String receiverVariableName,
                                       Map<String, String> fieldTypeByVariable,
                                       Map<String, String> methodVariableTypes) {
        if (receiverVariableName == null || receiverVariableName.isBlank()) {
            return null;
        }

        if (methodVariableTypes.containsKey(receiverVariableName)) {
            return methodVariableTypes.get(receiverVariableName);
        }

        return fieldTypeByVariable.get(receiverVariableName);
    }

    private String resolveComponentType(String simpleTypeName,
                                        Map<String, String> importedSimpleNames,
                                        Map<String, JavaComponent> bySimpleName) {
        if (simpleTypeName == null || simpleTypeName.isBlank()) {
            return null;
        }

        if (bySimpleName.containsKey(simpleTypeName)) {
            return simpleTypeName;
        }

        if (importedSimpleNames.containsKey(simpleTypeName) && bySimpleName.containsKey(simpleTypeName)) {
            return simpleTypeName;
        }

        return null;
    }

    private void addEdge(List<JavaDependencyEdge> edges,
                         Set<String> seen,
                         String dependencyClassName,
                         String dependentClassName,
                         String evidenceType,
                         String evidenceText,
                         String dependentMethodName,
                         String dependencyMethodName) {
        String key = dependencyClassName + "->" + dependentClassName
                + "#" + evidenceType
                + "#" + safe(dependentMethodName)
                + "#" + safe(dependencyMethodName);

        if (!seen.add(key)) {
            return;
        }

        edges.add(new JavaDependencyEdge(
                dependencyClassName,
                dependentClassName,
                evidenceType,
                evidenceText,
                dependentMethodName,
                dependencyMethodName
        ));
    }

    private int countChar(String line, char target) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class ConstructorMatch {
        private final Map<String, String> parameterTypeByVariable;

        private ConstructorMatch(Map<String, String> parameterTypeByVariable) {
            this.parameterTypeByVariable = parameterTypeByVariable;
        }
    }

    private static class MethodMatch {
        private final String methodName;
        private final Map<String, String> parameterTypeByVariable;

        private MethodMatch(String methodName, Map<String, String> parameterTypeByVariable) {
            this.methodName = methodName;
            this.parameterTypeByVariable = parameterTypeByVariable;
        }
    }

    private static class LocalVariableMatch {
        private final String typeName;
        private final String variableName;

        private LocalVariableMatch(String typeName, String variableName) {
            this.typeName = typeName;
            this.variableName = variableName;
        }
    }

    private static class MethodCallMatch {
        private final String receiverVariableName;
        private final String calledMethodName;

        private MethodCallMatch(String receiverVariableName, String calledMethodName) {
            this.receiverVariableName = receiverVariableName;
            this.calledMethodName = calledMethodName;
        }
    }
}