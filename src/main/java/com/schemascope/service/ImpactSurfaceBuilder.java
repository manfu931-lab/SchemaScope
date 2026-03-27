package com.schemascope.service;

import com.schemascope.domain.ApiEndpointImpact;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.ImpactSurfaceSummary;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.TestImpactHint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class ImpactSurfaceBuilder {

    private static final Pattern CLASS_REQUEST_MAPPING_PATTERN =
            Pattern.compile("@RequestMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"");

    private static final Pattern GET_MAPPING_PATTERN =
            Pattern.compile("@GetMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"?\\)");
    private static final Pattern POST_MAPPING_PATTERN =
            Pattern.compile("@PostMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"?\\)");
    private static final Pattern PUT_MAPPING_PATTERN =
            Pattern.compile("@PutMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"?\\)");
    private static final Pattern DELETE_MAPPING_PATTERN =
            Pattern.compile("@DeleteMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"?\\)");
    private static final Pattern PATCH_MAPPING_PATTERN =
            Pattern.compile("@PatchMapping\\s*\\((?:value\\s*=\\s*|path\\s*=\\s*)?\"([^\"]*)\"?\\)");
    private static final Pattern REQUEST_MAPPING_METHOD_PATTERN =
            Pattern.compile("@RequestMapping\\s*\\((.*)\\)");

    public ImpactSurfaceSummary build(String projectPath, List<ImpactResult> results) throws IOException {
        ImpactSurfaceSummary summary = new ImpactSurfaceSummary();

        if (projectPath == null || projectPath.isBlank() || results == null || results.isEmpty()) {
            return summary;
        }

        Path projectRoot = Path.of(projectPath).toAbsolutePath().normalize();

        Map<String, Path> mainClassFiles = indexJavaFiles(projectRoot.resolve(Path.of("src", "main", "java")));
        Map<String, Path> testClassFiles = indexTestFiles(projectRoot);

        summary.setImpactedEndpoints(buildImpactedEndpoints(results, mainClassFiles));
        summary.setSuggestedTests(buildSuggestedTests(results, testClassFiles));

        return summary;
    }

    private List<ApiEndpointImpact> buildImpactedEndpoints(List<ImpactResult> results,
                                                           Map<String, Path> mainClassFiles) throws IOException {
        List<ApiEndpointImpact> endpoints = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();

        for (ImpactResult result : results) {
            if (!isControllerType(result.getAffectedType())) {
                continue;
            }

            Path controllerFile = mainClassFiles.get(result.getAffectedObject());
            if (controllerFile == null || !Files.exists(controllerFile)) {
                continue;
            }

            List<ApiEndpointImpact> parsed = parseControllerEndpoints(controllerFile, result);
            for (ApiEndpointImpact endpoint : parsed) {
                String key = endpoint.getOwnerController() + "#" + endpoint.getHttpMethod() + "#" + endpoint.getPath();
                if (seen.add(key)) {
                    endpoints.add(endpoint);
                }
            }
        }

        return endpoints;
    }

    private List<TestImpactHint> buildSuggestedTests(List<ImpactResult> results,
                                                     Map<String, Path> testClassFiles) throws IOException {
        List<TestImpactHint> hints = new ArrayList<>();
        LinkedHashSet<String> seen = new LinkedHashSet<>();

        Map<String, String> testSources = new LinkedHashMap<>();
        for (Map.Entry<String, Path> entry : testClassFiles.entrySet()) {
            if (Files.exists(entry.getValue())) {
                testSources.put(entry.getKey(), Files.readString(entry.getValue(), StandardCharsets.UTF_8));
            }
        }

        for (ImpactResult result : results) {
            boolean matchedAnyExistingTest = false;

            for (Map.Entry<String, Path> entry : testClassFiles.entrySet()) {
                String testClassName = entry.getKey();
                String source = testSources.getOrDefault(testClassName, "");

                boolean fileNameMatch = testClassName.toLowerCase().contains(result.getAffectedObject().toLowerCase());
                boolean contentMatch = source.contains(result.getAffectedObject());

                if (!fileNameMatch && !contentMatch) {
                    continue;
                }

                matchedAnyExistingTest = true;

                String reason = fileNameMatch
                        ? "existing test class name matches impacted component '" + result.getAffectedObject() + "'"
                        : "existing test source references impacted component '" + result.getAffectedObject() + "'";

                TestImpactHint hint = new TestImpactHint(
                        testClassName,
                        entry.getValue().toString(),
                        reason,
                        toTestPriority(result)
                );

                String key = hint.getTestClassName() + "#" + result.getAffectedObject();
                if (seen.add(key)) {
                    hints.add(hint);
                }
            }

            if (!matchedAnyExistingTest && shouldSuggestNewTest(result)) {
                TestImpactHint hint = new TestImpactHint(
                        result.getAffectedObject() + "Test",
                        null,
                        "no existing focused test matched impacted component '" + result.getAffectedObject()
                                + "', suggest adding or updating targeted regression test",
                        toTestPriority(result)
                );

                String key = hint.getTestClassName() + "#" + result.getAffectedObject();
                if (seen.add(key)) {
                    hints.add(hint);
                }
            }
        }

        return hints;
    }

    private List<ApiEndpointImpact> parseControllerEndpoints(Path controllerFile,
                                                             ImpactResult result) throws IOException {
        List<ApiEndpointImpact> endpoints = new ArrayList<>();
        List<String> lines = Files.readAllLines(controllerFile, StandardCharsets.UTF_8);

        String classPrefix = "";
        boolean classDeclared = false;
        String pendingMethod = null;
        String pendingPath = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (!classDeclared && line.startsWith("@RequestMapping")) {
                String parsedPrefix = extractRequestPath(line);
                if (parsedPrefix != null) {
                    classPrefix = parsedPrefix;
                }
            }

            if (line.contains(" class ") || line.startsWith("public class ") || line.startsWith("class ")) {
                classDeclared = true;
            }

            if (line.startsWith("@GetMapping")) {
                pendingMethod = "GET";
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (line.startsWith("@PostMapping")) {
                pendingMethod = "POST";
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (line.startsWith("@PutMapping")) {
                pendingMethod = "PUT";
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (line.startsWith("@DeleteMapping")) {
                pendingMethod = "DELETE";
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (line.startsWith("@PatchMapping")) {
                pendingMethod = "PATCH";
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (line.startsWith("@RequestMapping") && classDeclared) {
                pendingMethod = extractRequestMethod(line);
                pendingPath = extractRequestPath(line);
                continue;
            }

            if (pendingMethod != null && looksLikeMethodDeclaration(line)) {
                endpoints.add(new ApiEndpointImpact(
                        result.getAffectedObject(),
                        pendingMethod,
                        joinPath(classPrefix, pendingPath),
                        result.getRiskLevel(),
                        result.getRelationLevel()
                ));
                pendingMethod = null;
                pendingPath = null;
            }
        }

        return endpoints;
    }

    private boolean looksLikeMethodDeclaration(String line) {
        String trimmed = line.trim();
        return trimmed.contains("(")
                && trimmed.contains(")")
                && (trimmed.endsWith("{") || trimmed.endsWith(";"))
                && !trimmed.startsWith("if ")
                && !trimmed.startsWith("for ")
                && !trimmed.startsWith("while ")
                && !trimmed.startsWith("switch ")
                && !trimmed.startsWith("@");
    }

    private String extractRequestPath(String annotationLine) {
        Matcher getMatcher = GET_MAPPING_PATTERN.matcher(annotationLine);
        if (getMatcher.find()) {
            return getMatcher.group(1);
        }

        Matcher postMatcher = POST_MAPPING_PATTERN.matcher(annotationLine);
        if (postMatcher.find()) {
            return postMatcher.group(1);
        }

        Matcher putMatcher = PUT_MAPPING_PATTERN.matcher(annotationLine);
        if (putMatcher.find()) {
            return putMatcher.group(1);
        }

        Matcher deleteMatcher = DELETE_MAPPING_PATTERN.matcher(annotationLine);
        if (deleteMatcher.find()) {
            return deleteMatcher.group(1);
        }

        Matcher patchMatcher = PATCH_MAPPING_PATTERN.matcher(annotationLine);
        if (patchMatcher.find()) {
            return patchMatcher.group(1);
        }

        Matcher requestMatcher = CLASS_REQUEST_MAPPING_PATTERN.matcher(annotationLine);
        if (requestMatcher.find()) {
            return requestMatcher.group(1);
        }

        if (annotationLine.contains("()")) {
            return "";
        }

        return null;
    }

    private String extractRequestMethod(String annotationLine) {
        Matcher matcher = REQUEST_MAPPING_METHOD_PATTERN.matcher(annotationLine);
        if (!matcher.find()) {
            return "REQUEST";
        }

        String content = matcher.group(1);
        if (content.contains("RequestMethod.GET")) {
            return "GET";
        }
        if (content.contains("RequestMethod.POST")) {
            return "POST";
        }
        if (content.contains("RequestMethod.PUT")) {
            return "PUT";
        }
        if (content.contains("RequestMethod.DELETE")) {
            return "DELETE";
        }
        if (content.contains("RequestMethod.PATCH")) {
            return "PATCH";
        }
        return "REQUEST";
    }

    private String joinPath(String classPrefix, String methodPath) {
        String prefix = normalizeSegment(classPrefix);
        String suffix = normalizeSegment(methodPath);

        if (prefix.isBlank() && suffix.isBlank()) {
            return "/";
        }
        if (prefix.isBlank()) {
            return suffix;
        }
        if (suffix.isBlank()) {
            return prefix;
        }

        String joined = prefix + "/" + suffix;
        return joined.replaceAll("/{2,}", "/");
    }

    private String normalizeSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            return "";
        }

        String trimmed = segment.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        if (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean isControllerType(String affectedType) {
        return "CONTROLLER".equalsIgnoreCase(affectedType)
                || "REST_CONTROLLER".equalsIgnoreCase(affectedType);
    }

    private boolean shouldSuggestNewTest(ImpactResult result) {
        return "SERVICE".equalsIgnoreCase(result.getAffectedType())
                || "CONTROLLER".equalsIgnoreCase(result.getAffectedType())
                || "REST_CONTROLLER".equalsIgnoreCase(result.getAffectedType())
                || "REPOSITORY".equalsIgnoreCase(result.getAffectedType());
    }

    private RiskLevel toTestPriority(ImpactResult result) {
        if (result.getRelationLevel() == ImpactRelationLevel.DIRECT && result.getRiskLevel() == RiskLevel.HIGH) {
            return RiskLevel.HIGH;
        }
        if (result.getRelationLevel() == ImpactRelationLevel.DIRECT) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private Map<String, Path> indexTestFiles(Path projectRoot) throws IOException {
        Map<String, Path> map = new LinkedHashMap<>();

        mergeJavaFiles(map, projectRoot.resolve(Path.of("src", "test", "java")));

        try (Stream<Path> stream = Files.walk(projectRoot)) {
            stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith("Test.java"))
                    .forEach(path -> putSimpleNameIfAbsent(map, path));
        }

        return map;
    }

    private Map<String, Path> indexJavaFiles(Path root) throws IOException {
        Map<String, Path> map = new LinkedHashMap<>();
        mergeJavaFiles(map, root);
        return map;
    }

    private void mergeJavaFiles(Map<String, Path> target, Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .forEach(path -> putSimpleNameIfAbsent(target, path));
        }
    }

    private void putSimpleNameIfAbsent(Map<String, Path> map, Path path) {
        String fileName = path.getFileName().toString();
        String simpleName = fileName.substring(0, fileName.length() - ".java".length());
        map.putIfAbsent(simpleName, path.toAbsolutePath().normalize());
    }
}