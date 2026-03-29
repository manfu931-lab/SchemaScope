package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SqlImpactCandidate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SqlImpactPropagator {

    private static final int MAX_DEPTH = 2;

    private final JavaDependencyGraphBuilder javaDependencyGraphBuilder;

    private static final Pattern FIELD_DEPENDENCY_PATTERN = Pattern.compile(
            "^(?:private|protected|public)\\s+(?:static\\s+)?(?:final\\s+)?([A-Z][A-Za-z0-9_]*)\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*;"
    );

    private static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            "^(?:public|protected|private|static|final|default|synchronized|abstract|native|strictfp|\\s)*" +
                    "[A-Za-z0-9_<>\\[\\],.? ]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^)]*\\)\\s*\\{$"
    );

    public List<ComponentImpactCandidate> propagate(List<SqlImpactCandidate> sqlCandidates,
                                                    JavaProjectScanResult scanResult) throws IOException {
        List<ComponentImpactCandidate> results = new ArrayList<>();

        if (sqlCandidates == null || sqlCandidates.isEmpty() || scanResult == null || scanResult.getComponents() == null) {
            return results;
        }

        Map<String, JavaComponent> componentsByName = indexComponents(scanResult);
        Map<String, String> sourceTextByClass = loadSourceText(scanResult);
        Map<String, ClassModel> classModels = buildClassModels(scanResult, sourceTextByClass, componentsByName.keySet());

        Map<String, ComponentImpactCandidate> bestCandidateByClass = new LinkedHashMap<>();
        Queue<PropagationState> queue = new ArrayDeque<>();
        Set<String> visitedMethodEdges = new LinkedHashSet<>();

        for (SqlImpactCandidate sqlCandidate : sqlCandidates) {
            if (sqlCandidate.getAccessPoint() == null) {
                continue;
            }

            String ownerClassName = sqlCandidate.getAccessPoint().getOwnerClassName();
            String ownerMethodName = sqlCandidate.getAccessPoint().getOwnerMethodName();
            if (ownerClassName == null || ownerClassName.isBlank()
                    || ownerMethodName == null || ownerMethodName.isBlank()) {
                continue;
            }

            JavaComponent ownerComponent = componentsByName.get(ownerClassName.toLowerCase());
            if (ownerComponent == null) {
                continue;
            }

            double seedScore = clamp(sqlCandidate.getScore() * 0.95);
            ImpactRelationLevel seedRelation = resolveSeedRelation(ownerComponent.getComponentType());

            List<String> seedEvidencePath = new ArrayList<>();
            seedEvidencePath.add("Matched SQL: " + sqlCandidate.getAccessPoint().getSqlId());
            seedEvidencePath.add("SQL source: " + sqlCandidate.getAccessPoint().getSourceType());
            seedEvidencePath.add("SQL owner: "
                    + sqlCandidate.getAccessPoint().getOwnerClassName()
                    + "." + sqlCandidate.getAccessPoint().getOwnerMethodName());
            seedEvidencePath.add("SQL snippet: " + compactSql(sqlCandidate.getAccessPoint().getRawSql()));
            seedEvidencePath.add("Match reason: " + sqlCandidate.getReason());

            ComponentImpactCandidate seedCandidate = new ComponentImpactCandidate(
                    ownerComponent,
                    seedScore,
                    "owns matched SQL access point '" + sqlCandidate.getAccessPoint().getSqlId()
                            + "'; " + sqlCandidate.getReason(),
                    seedRelation,
                    seedEvidencePath
            );

            upsert(bestCandidateByClass, seedCandidate);

            queue.offer(new PropagationState(
                    ownerClassName,
                    ownerMethodName,
                    seedScore,
                    0,
                    ownerClassName,
                    seedEvidencePath
            ));
        }

        while (!queue.isEmpty()) {
            PropagationState current = queue.poll();

            if (current.depth >= MAX_DEPTH) {
                continue;
            }

            for (ClassModel callerClassModel : classModels.values()) {
                if (callerClassModel.className.equals(current.className)) {
                    continue;
                }

                List<MethodCall> methodCalls = extractMethodCalls(callerClassModel);
                for (MethodCall methodCall : methodCalls) {
                    if (!methodCall.targetClass.equals(current.className)
                            || !methodCall.targetMethod.equals(current.methodName)) {
                        continue;
                    }

                    String edgeKey = methodCall.callerClass + "#" + methodCall.callerMethod
                            + "->" + methodCall.targetClass + "#" + methodCall.targetMethod;
                    if (!visitedMethodEdges.add(edgeKey)) {
                        continue;
                    }

                    double propagatedScore = scoreForNextHop(
                            current.score,
                            current.depth + 1,
                            callerClassModel.component.getComponentType(),
                            callerClassModel.className,
                            current.className
                    );

                    if (propagatedScore < 0.60) {
                        continue;
                    }

                    ImpactRelationLevel relationLevel = resolvePropagatedRelation(
                            current.depth + 1,
                            callerClassModel.component.getComponentType()
                    );

                    List<String> propagatedEvidencePath = new ArrayList<>(current.evidencePath);
                    propagatedEvidencePath.add("Propagation: " + methodCall.callerClass + " references " + methodCall.targetClass);
                    propagatedEvidencePath.add("Method propagation: "
                            + methodCall.callerClass + "." + methodCall.callerMethod
                            + " calls " + methodCall.targetClass + "." + methodCall.targetMethod);
                    propagatedEvidencePath.add("Dependency evidence: " + methodCall.evidenceLine);

                    ComponentImpactCandidate candidate = new ComponentImpactCandidate(
                            callerClassModel.component,
                            propagatedScore,
                            "method '" + methodCall.callerMethod + "' calls '"
                                    + methodCall.targetClass + "." + methodCall.targetMethod
                                    + "' which traces back to matched SQL owner '"
                                    + current.rootSqlOwnerClass + "'",
                            relationLevel,
                            propagatedEvidencePath
                    );

                    upsert(bestCandidateByClass, candidate);

                    queue.offer(new PropagationState(
                            methodCall.callerClass,
                            methodCall.callerMethod,
                            propagatedScore,
                            current.depth + 1,
                            current.rootSqlOwnerClass,
                            propagatedEvidencePath
                    ));
                }
            }
        }

        results.addAll(bestCandidateByClass.values());
        results.sort(Comparator.comparing(ComponentImpactCandidate::getScore).reversed()
                .thenComparing(candidate -> candidate.getComponent().getClassName()));

        return results;
    }

    private Map<String, JavaComponent> indexComponents(JavaProjectScanResult scanResult) {
        Map<String, JavaComponent> map = new LinkedHashMap<>();
        for (JavaComponent component : scanResult.getComponents()) {
            if (component.getClassName() == null) {
                continue;
            }
            map.put(component.getClassName().toLowerCase(), component);
        }
        return map;
    }

    private Map<String, String> loadSourceText(JavaProjectScanResult scanResult) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();

        for (JavaComponent component : scanResult.getComponents()) {
            if (component.getClassName() == null || component.getFilePath() == null) {
                continue;
            }

            Path filePath = Path.of(component.getFilePath());
            if (!Files.exists(filePath)) {
                continue;
            }

            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            map.put(component.getClassName(), content);
        }

        return map;
    }

    public SqlImpactPropagator() {
        this(new JavaDependencyGraphBuilder());
    }
    
    public SqlImpactPropagator(JavaDependencyGraphBuilder javaDependencyGraphBuilder) {
        this.javaDependencyGraphBuilder = javaDependencyGraphBuilder;
    }

    private Map<String, ClassModel> buildClassModels(JavaProjectScanResult scanResult,
                                                     Map<String, String> sourceTextByClass,
                                                     Set<String> knownComponentNames) {
        Map<String, ClassModel> models = new LinkedHashMap<>();

        for (JavaComponent component : scanResult.getComponents()) {
            String source = sourceTextByClass.get(component.getClassName());
            if (source == null || source.isBlank()) {
                continue;
            }

            ClassModel model = new ClassModel(
                    component,
                    component.getClassName(),
                    extractFieldDependencies(source, knownComponentNames),
                    extractMethods(source)
            );

            models.put(component.getClassName(), model);
        }

        return models;
    }

    private Map<String, String> extractFieldDependencies(String source, Set<String> knownComponentNames) {
        Map<String, String> dependencies = new LinkedHashMap<>();
        String[] lines = source.split("\\R");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()
                    || line.startsWith("//")
                    || line.startsWith("package ")
                    || line.startsWith("import ")
                    || line.startsWith("@")) {
                continue;
            }

            Matcher matcher = FIELD_DEPENDENCY_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }

            String typeName = matcher.group(1);
            String fieldName = matcher.group(2);

            if (knownComponentNames.contains(typeName.toLowerCase())) {
                dependencies.put(fieldName, typeName);
            }
        }

        return dependencies;
    }

    private List<MethodModel> extractMethods(String source) {
        List<MethodModel> methods = new ArrayList<>();
        String[] lines = source.split("\\R", -1);

        boolean collecting = false;
        String currentMethodName = null;
        StringBuilder bodyBuilder = null;
        int braceDepth = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (!collecting) {
                if (!looksLikeMethodDeclaration(line)) {
                    continue;
                }

                currentMethodName = extractMethodName(line);
                if (currentMethodName == null) {
                    continue;
                }

                collecting = true;
                bodyBuilder = new StringBuilder();
                bodyBuilder.append(rawLine).append('\n');
                braceDepth = countChar(rawLine, '{') - countChar(rawLine, '}');

                if (braceDepth == 0) {
                    methods.add(new MethodModel(currentMethodName, bodyBuilder.toString()));
                    collecting = false;
                    currentMethodName = null;
                    bodyBuilder = null;
                }
                continue;
            }

            bodyBuilder.append(rawLine).append('\n');
            braceDepth += countChar(rawLine, '{');
            braceDepth -= countChar(rawLine, '}');

            if (braceDepth == 0) {
                methods.add(new MethodModel(currentMethodName, bodyBuilder.toString()));
                collecting = false;
                currentMethodName = null;
                bodyBuilder = null;
            }
        }

        return methods;
    }

    private boolean looksLikeMethodDeclaration(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()
                || trimmed.startsWith("@")
                || trimmed.startsWith("if ")
                || trimmed.startsWith("if(")
                || trimmed.startsWith("for ")
                || trimmed.startsWith("for(")
                || trimmed.startsWith("while ")
                || trimmed.startsWith("while(")
                || trimmed.startsWith("switch ")
                || trimmed.startsWith("switch(")
                || trimmed.startsWith("catch ")
                || trimmed.startsWith("catch(")
                || trimmed.startsWith("return ")
                || trimmed.startsWith("new ")) {
            return false;
        }

        return METHOD_DECLARATION_PATTERN.matcher(trimmed).matches();
    }

    private String extractMethodName(String line) {
        Matcher matcher = METHOD_DECLARATION_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private List<MethodCall> extractMethodCalls(ClassModel classModel) {
        List<MethodCall> methodCalls = new ArrayList<>();

        for (MethodModel methodModel : classModel.methods) {
            String[] lines = methodModel.body.split("\\R");

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                for (Map.Entry<String, String> dependency : classModel.dependenciesByFieldName.entrySet()) {
                    String fieldName = dependency.getKey();
                    String targetClass = dependency.getValue();

                    Pattern callPattern = Pattern.compile(
                            "\\b" + Pattern.quote(fieldName) + "\\s*\\.\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*\\("
                    );

                    Matcher matcher = callPattern.matcher(line);
                    while (matcher.find()) {
                        String targetMethod = matcher.group(1);
                        methodCalls.add(new MethodCall(
                                classModel.className,
                                methodModel.name,
                                targetClass,
                                targetMethod,
                                line
                        ));
                    }
                }
            }
        }

        return methodCalls;
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

    private void upsert(Map<String, ComponentImpactCandidate> bestCandidateByClass,
                        ComponentImpactCandidate incoming) {
        String className = incoming.getComponent().getClassName();
        ComponentImpactCandidate existing = bestCandidateByClass.get(className);

        if (existing == null || incoming.getScore() > existing.getScore()) {
            bestCandidateByClass.put(className, incoming);
            return;
        }

        if (Math.abs(incoming.getScore() - existing.getScore()) < 0.0001) {
            mergeIfMissing(existing, incoming.getReason());
            mergeEvidencePath(existing, incoming.getEvidencePath());
        }
    }

    private void mergeIfMissing(ComponentImpactCandidate existing, String incomingReason) {
        if (incomingReason == null || incomingReason.isBlank()) {
            return;
        }

        if (existing.getReason() == null || existing.getReason().isBlank()) {
            existing.setReason(incomingReason);
            return;
        }

        if (!existing.getReason().contains(incomingReason)) {
            existing.setReason(existing.getReason() + "; " + incomingReason);
        }
    }

    private void mergeEvidencePath(ComponentImpactCandidate existing, List<String> incomingEvidencePath) {
        if (incomingEvidencePath == null || incomingEvidencePath.isEmpty()) {
            return;
        }

        List<String> merged = new ArrayList<>(existing.getEvidencePath());
        for (String step : incomingEvidencePath) {
            if (!merged.contains(step)) {
                merged.add(step);
            }
        }
        existing.setEvidencePath(merged);
    }

    private double scoreForNextHop(double parentScore,
                                   int nextDepth,
                                   JavaComponentType type,
                                   String callerClassName,
                                   String calleeClassName) {
        double score = parentScore - 0.10;

        if (type == JavaComponentType.SERVICE) {
            score += 0.04;
        } else if (type == JavaComponentType.CONTROLLER || type == JavaComponentType.REST_CONTROLLER) {
            score -= 0.02;
        }

        if (nextDepth >= 2) {
            score -= 0.03;
        }

        if (!sameDomain(callerClassName, calleeClassName)) {
            score -= 0.18;
        }

        if (!sameDomain(callerClassName, calleeClassName)
            && (type == JavaComponentType.CONTROLLER || type == JavaComponentType.REST_CONTROLLER)) {
        score -= 0.08;
        }

        return clamp(score);
    }

    private boolean sameDomain(String left, String right) {
        return normalizeDomainRoot(left).equals(normalizeDomainRoot(right));
    }

    private String normalizeDomainRoot(String className) {
        if (className == null || className.isBlank()) {
            return "";
        }

        String normalized = className
                .replace("RestController", "")
                .replace("Controller", "")
                .replace("Service", "")
                .replace("Repository", "")
                .replace("Dao", "")
                .replace("Mapper", "")
                .replace("Impl", "");

        return normalized.toLowerCase();
    }

    private ImpactRelationLevel resolveSeedRelation(JavaComponentType type) {
        return switch (type) {
            case ENTITY, REPOSITORY -> ImpactRelationLevel.DIRECT;
            case SERVICE, CONTROLLER, REST_CONTROLLER -> ImpactRelationLevel.INDIRECT;
        };
    }

    private ImpactRelationLevel resolvePropagatedRelation(int depth, JavaComponentType type) {
        if (depth == 1 && type == JavaComponentType.SERVICE) {
            return ImpactRelationLevel.DIRECT;
        }
        return ImpactRelationLevel.INDIRECT;
    }

    private double clamp(double score) {
        if (score < 0.0) {
            return 0.0;
        }
        return Math.min(score, 1.0);
    }

    private String compactSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static class PropagationState {
        private final String className;
        private final String methodName;
        private final double score;
        private final int depth;
        private final String rootSqlOwnerClass;
        private final List<String> evidencePath;

        private PropagationState(String className,
                                 String methodName,
                                 double score,
                                 int depth,
                                 String rootSqlOwnerClass,
                                 List<String> evidencePath) {
            this.className = className;
            this.methodName = methodName;
            this.score = score;
            this.depth = depth;
            this.rootSqlOwnerClass = rootSqlOwnerClass;
            this.evidencePath = new ArrayList<>(evidencePath);
        }
    }

    private static class ClassModel {
        private final JavaComponent component;
        private final String className;
        private final Map<String, String> dependenciesByFieldName;
        private final List<MethodModel> methods;

        private ClassModel(JavaComponent component,
                           String className,
                           Map<String, String> dependenciesByFieldName,
                           List<MethodModel> methods) {
            this.component = component;
            this.className = className;
            this.dependenciesByFieldName = dependenciesByFieldName;
            this.methods = methods;
        }
    }

    private static class MethodModel {
        private final String name;
        private final String body;

        private MethodModel(String name, String body) {
            this.name = name;
            this.body = body;
        }
    }

    private static class MethodCall {
        private final String callerClass;
        private final String callerMethod;
        private final String targetClass;
        private final String targetMethod;
        private final String evidenceLine;

        private MethodCall(String callerClass,
                           String callerMethod,
                           String targetClass,
                           String targetMethod,
                           String evidenceLine) {
            this.callerClass = callerClass;
            this.callerMethod = callerMethod;
            this.targetClass = targetClass;
            this.targetMethod = targetMethod;
            this.evidenceLine = evidenceLine;
        }
    }
}