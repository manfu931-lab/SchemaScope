package com.schemascope.parser;

import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlSourceType;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class SqlAccessExtractor {

    private static final Pattern CLASS_PATTERN =
            Pattern.compile("\\b(class|interface|record)\\s+([A-Za-z_][A-Za-z0-9_]*)");

    private static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            "^(?:public|protected|private|static|final|default|synchronized|abstract|native|strictfp|\\s)*" +
                    "[A-Za-z0-9_<>\\[\\],? ]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^)]*\\)\\s*(?:\\{|;)$"
    );

    private static final Pattern QUOTED_STRING_PATTERN =
            Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(?i)\\b(from|join|update|into)\\s+[`\"]?([a-zA-Z0-9_]+)[`\"]?");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-z0-9_]+");

    private static final Pattern STRING_ASSIGNMENT_PATTERN =
            Pattern.compile("^(?:final\\s+)?String\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*(.+);$");

    private static final Pattern STRING_ASSIGNMENT_START_PATTERN =
            Pattern.compile("^(?:final\\s+)?String\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$");

    private static final Pattern STRING_REASSIGNMENT_PATTERN =
            Pattern.compile("^([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*(.+);$");

    private static final Pattern STRING_REASSIGNMENT_START_PATTERN =
            Pattern.compile("^([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$");

    private static final Pattern STRING_BUILDER_INIT_PATTERN =
            Pattern.compile("^(?:final\\s+)?StringBuilder\\s+([a-zA-Z_][A-Za-z0-9_]*)\\s*=\\s*new\\s+StringBuilder\\s*\\((.*)\\)\\s*;$");

    private static final Pattern STRING_BUILDER_APPEND_PATTERN =
            Pattern.compile("^([a-zA-Z_][A-Za-z0-9_]*)\\.append\\((.+)\\)\\s*;$");

    private static final Pattern TO_STRING_PATTERN =
            Pattern.compile("^([a-zA-Z_][A-Za-z0-9_]*)\\.toString\\(\\)$");

    private static final Pattern IDENTIFIER_PATTERN =
            Pattern.compile("^[a-zA-Z_][A-Za-z0-9_]*$");

    private final MyBatisXmlSqlExtractor myBatisXmlSqlExtractor;

    public SqlAccessExtractor() {
        this(new MyBatisXmlSqlExtractor());
    }

    public SqlAccessExtractor(MyBatisXmlSqlExtractor myBatisXmlSqlExtractor) {
        this.myBatisXmlSqlExtractor = myBatisXmlSqlExtractor;
    }

    public List<SqlAccessPoint> extractFromProject(String projectRootPath) throws IOException {
        List<SqlAccessPoint> results = new ArrayList<>();

        Path projectRoot = Path.of(projectRootPath).toAbsolutePath().normalize();
        if (!Files.exists(projectRoot)) {
            throw new IllegalArgumentException("Project root does not exist: " + projectRoot);
        }

        Path javaSourceRoot = projectRoot.resolve(Path.of("src", "main", "java"));
        if (!Files.exists(javaSourceRoot)) {
            javaSourceRoot = projectRoot;
        }

        if (Files.exists(javaSourceRoot)) {
            try (Stream<Path> pathStream = Files.walk(javaSourceRoot)) {
                pathStream
                        .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                        .forEach(path -> {
                            try {
                                results.addAll(extractFromJavaFile(path));
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to parse java file: " + path, e);
                            }
                        });
            }
        }

        Path resourcesRoot = projectRoot.resolve(Path.of("src", "main", "resources"));
        if (Files.exists(resourcesRoot)) {
            try (Stream<Path> pathStream = Files.walk(resourcesRoot)) {
                pathStream
                        .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".xml"))
                        .forEach(path -> {
                            try {
                                results.addAll(extractFromXmlFile(path));
                            } catch (IOException | ParserConfigurationException | SAXException e) {
                                throw new RuntimeException("Failed to parse xml file: " + path, e);
                            }
                        });
            }
        }

        return results;
    }

    private List<SqlAccessPoint> extractFromXmlFile(Path xmlFile)
            throws IOException, ParserConfigurationException, SAXException {
        return myBatisXmlSqlExtractor.extractFromXmlFile(xmlFile);
    }

    List<SqlAccessPoint> extractFromJavaFile(Path javaFile) throws IOException {
        List<SqlAccessPoint> results = new ArrayList<>();
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        String currentClassName = null;
        String currentMethodName = null;
        int braceDepth = 0;
        int methodBraceDepth = -1;

        String pendingQuerySql = null;
        SqlSourceType pendingQueryType = null;
        StringBuilder queryAnnotationBuffer = null;

        StringBuilder jdbcCallBuffer = null;

        String pendingStringVariableName = null;
        StringBuilder pendingStringExpressionBuffer = null;

        Map<String, String> stringVariables = new LinkedHashMap<>();
        Map<String, StringBuilder> builderVariables = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (queryAnnotationBuffer != null) {
                queryAnnotationBuffer.append(' ').append(line);
                if (line.contains(")")) {
                    String queryAnnotation = queryAnnotationBuffer.toString();
                    pendingQuerySql = extractQuotedSql(queryAnnotation);
                    pendingQueryType = isNativeQuery(queryAnnotation)
                            ? SqlSourceType.NATIVE_QUERY
                            : SqlSourceType.JPA_QUERY;
                    queryAnnotationBuffer = null;
                }

                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                continue;
            }

            if (jdbcCallBuffer != null) {
                jdbcCallBuffer.append(' ').append(line);
                if (line.contains(";")) {
                    String statement = jdbcCallBuffer.toString();
                    processJdbcStatement(statement, currentClassName, currentMethodName, javaFile,
                            stringVariables, builderVariables, results);
                    jdbcCallBuffer = null;
                }

                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                if (methodBraceDepth > 0 && braceDepth < methodBraceDepth) {
                    currentMethodName = null;
                    methodBraceDepth = -1;
                    stringVariables.clear();
                    builderVariables.clear();
                    pendingStringVariableName = null;
                    pendingStringExpressionBuffer = null;
                }
                continue;
            }

            if (pendingStringExpressionBuffer != null) {
                pendingStringExpressionBuffer.append(' ').append(line);
                if (line.endsWith(";")) {
                    String fullExpression = pendingStringExpressionBuffer.toString().trim();
                    if (fullExpression.endsWith(";")) {
                        fullExpression = fullExpression.substring(0, fullExpression.length() - 1).trim();
                    }

                    String resolved = resolveExpression(fullExpression, stringVariables, builderVariables);
                    if (resolved != null && pendingStringVariableName != null) {
                        stringVariables.put(pendingStringVariableName, resolved);
                    }

                    pendingStringVariableName = null;
                    pendingStringExpressionBuffer = null;
                }

                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                if (methodBraceDepth > 0 && braceDepth < methodBraceDepth) {
                    currentMethodName = null;
                    methodBraceDepth = -1;
                    stringVariables.clear();
                    builderVariables.clear();
                    pendingStringVariableName = null;
                    pendingStringExpressionBuffer = null;
                }
                continue;
            }

            if (line.isEmpty() || line.startsWith("//")) {
                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                continue;
            }

            String detectedClassName = extractClassName(line);
            if (detectedClassName != null) {
                currentClassName = detectedClassName;
            }

            if (line.startsWith("@Query")) {
                queryAnnotationBuffer = new StringBuilder(line);
                if (line.contains(")")) {
                    String queryAnnotation = queryAnnotationBuffer.toString();
                    pendingQuerySql = extractQuotedSql(queryAnnotation);
                    pendingQueryType = isNativeQuery(queryAnnotation)
                            ? SqlSourceType.NATIVE_QUERY
                            : SqlSourceType.JPA_QUERY;
                    queryAnnotationBuffer = null;
                }

                braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');
                continue;
            }

            if (looksLikeMethodDeclaration(line)) {
                currentMethodName = extractMethodName(line);
                stringVariables.clear();
                builderVariables.clear();
                pendingStringVariableName = null;
                pendingStringExpressionBuffer = null;

                int depthAfterLine = braceDepth + countChar(rawLine, '{') - countChar(rawLine, '}');
                methodBraceDepth = line.endsWith("{") ? depthAfterLine : -1;

                if (pendingQuerySql != null && currentMethodName != null) {
                    results.add(buildAccessPoint(
                            currentClassName,
                            currentMethodName,
                            javaFile,
                            pendingQuerySql,
                            pendingQueryType
                    ));
                    pendingQuerySql = null;
                    pendingQueryType = null;
                }

                braceDepth = depthAfterLine;
                if (methodBraceDepth > 0 && braceDepth < methodBraceDepth) {
                    currentMethodName = null;
                    methodBraceDepth = -1;
                    stringVariables.clear();
                    builderVariables.clear();
                }
                continue;
            }

            if (currentMethodName != null) {
                PendingStringAssignment pendingAssignment =
                        parseStringAssignment(line, stringVariables, builderVariables);

                if (pendingAssignment != null) {
                    pendingStringVariableName = pendingAssignment.variableName;
                    pendingStringExpressionBuffer = new StringBuilder(pendingAssignment.expressionStart);
                } else {
                    parseStringBuilder(line, stringVariables, builderVariables);
                }

                if (containsJdbcTemplateCall(line)) {
                    if (line.contains(";")) {
                        processJdbcStatement(line, currentClassName, currentMethodName, javaFile,
                                stringVariables, builderVariables, results);
                    } else {
                        jdbcCallBuffer = new StringBuilder(line);
                    }
                }
            }

            braceDepth += countChar(rawLine, '{') - countChar(rawLine, '}');

            if (methodBraceDepth > 0 && braceDepth < methodBraceDepth) {
                currentMethodName = null;
                methodBraceDepth = -1;
                stringVariables.clear();
                builderVariables.clear();
                pendingStringVariableName = null;
                pendingStringExpressionBuffer = null;
            }
        }

        return results;
    }

    private void processJdbcStatement(String statement,
                                      String currentClassName,
                                      String currentMethodName,
                                      Path javaFile,
                                      Map<String, String> stringVariables,
                                      Map<String, StringBuilder> builderVariables,
                                      List<SqlAccessPoint> results) {
        if (currentMethodName == null || !containsJdbcTemplateCall(statement)) {
            return;
        }

        String firstArgument = extractFirstArgument(statement);
        if (firstArgument == null || firstArgument.isBlank()) {
            return;
        }

        String sql = resolveExpression(firstArgument, stringVariables, builderVariables);
        if (sql == null || sql.isBlank()) {
            return;
        }

        results.add(buildAccessPoint(
                currentClassName,
                currentMethodName,
                javaFile,
                sql,
                SqlSourceType.JDBC_TEMPLATE
        ));
    }

    private PendingStringAssignment parseStringAssignment(String line,
                                                          Map<String, String> stringVariables,
                                                          Map<String, StringBuilder> builderVariables) {
        Matcher assignmentMatcher = STRING_ASSIGNMENT_PATTERN.matcher(line);
        if (assignmentMatcher.matches()) {
            String variable = assignmentMatcher.group(1);
            String expression = assignmentMatcher.group(2);
            String resolved = resolveExpression(expression, stringVariables, builderVariables);
            if (resolved != null) {
                stringVariables.put(variable, resolved);
            }
            return null;
        }

        Matcher assignmentStartMatcher = STRING_ASSIGNMENT_START_PATTERN.matcher(line);
        if (assignmentStartMatcher.matches() && !line.endsWith(";")) {
            return new PendingStringAssignment(
                    assignmentStartMatcher.group(1),
                    assignmentStartMatcher.group(2)
            );
        }

        Matcher reassignmentMatcher = STRING_REASSIGNMENT_PATTERN.matcher(line);
        if (reassignmentMatcher.matches()) {
            String variable = reassignmentMatcher.group(1);
            String expression = reassignmentMatcher.group(2);

            if (!stringVariables.containsKey(variable)) {
                return null;
            }

            String resolved = resolveExpression(expression, stringVariables, builderVariables);
            if (resolved != null) {
                stringVariables.put(variable, resolved);
            }
            return null;
        }

        Matcher reassignmentStartMatcher = STRING_REASSIGNMENT_START_PATTERN.matcher(line);
        if (reassignmentStartMatcher.matches() && !line.endsWith(";")) {
            String variable = reassignmentStartMatcher.group(1);
            if (stringVariables.containsKey(variable)) {
                return new PendingStringAssignment(
                        variable,
                        reassignmentStartMatcher.group(2)
                );
            }
        }

        return null;
    }

    private void parseStringBuilder(String line,
                                    Map<String, String> stringVariables,
                                    Map<String, StringBuilder> builderVariables) {
        Matcher initMatcher = STRING_BUILDER_INIT_PATTERN.matcher(line);
        if (initMatcher.matches()) {
            String variable = initMatcher.group(1);
            String initialExpression = initMatcher.group(2).trim();
            StringBuilder builder = new StringBuilder();

            if (!initialExpression.isBlank()) {
                String resolved = resolveExpression(initialExpression, stringVariables, builderVariables);
                if (resolved != null) {
                    builder.append(resolved);
                }
            }

            builderVariables.put(variable, builder);
            return;
        }

        Matcher appendMatcher = STRING_BUILDER_APPEND_PATTERN.matcher(line);
        if (appendMatcher.matches()) {
            String variable = appendMatcher.group(1);
            String expression = appendMatcher.group(2);

            StringBuilder builder = builderVariables.get(variable);
            if (builder == null) {
                return;
            }

            String resolved = resolveExpression(expression, stringVariables, builderVariables);
            if (resolved != null) {
                builder.append(resolved);
            }
        }
    }

    private String resolveExpression(String expression,
                                     Map<String, String> stringVariables,
                                     Map<String, StringBuilder> builderVariables) {
        if (expression == null) {
            return null;
        }

        String normalized = expression.trim();
        if (normalized.endsWith(";")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }

        Matcher toStringMatcher = TO_STRING_PATTERN.matcher(normalized);
        if (toStringMatcher.matches()) {
            String builderName = toStringMatcher.group(1);
            StringBuilder builder = builderVariables.get(builderName);
            return builder == null ? null : builder.toString();
        }

        if (IDENTIFIER_PATTERN.matcher(normalized).matches()) {
            if (stringVariables.containsKey(normalized)) {
                return stringVariables.get(normalized);
            }
            if (builderVariables.containsKey(normalized)) {
                return builderVariables.get(normalized).toString();
            }
        }

        List<String> parts = splitByPlus(normalized);
        if (parts.size() > 1) {
            StringBuilder merged = new StringBuilder();
            boolean resolvedAny = false;

            for (String part : parts) {
                String resolved = resolveSingleTerm(part, stringVariables, builderVariables);
                if (resolved != null) {
                    merged.append(resolved);
                    resolvedAny = true;
                }
            }

            return resolvedAny ? merged.toString() : null;
        }

        return resolveSingleTerm(normalized, stringVariables, builderVariables);
    }

    private String resolveSingleTerm(String term,
                                     Map<String, String> stringVariables,
                                     Map<String, StringBuilder> builderVariables) {
        String normalized = term.trim();
        if (normalized.isBlank()) {
            return "";
        }

        Matcher toStringMatcher = TO_STRING_PATTERN.matcher(normalized);
        if (toStringMatcher.matches()) {
            String builderName = toStringMatcher.group(1);
            StringBuilder builder = builderVariables.get(builderName);
            return builder == null ? null : builder.toString();
        }

        if (IDENTIFIER_PATTERN.matcher(normalized).matches()) {
            if (stringVariables.containsKey(normalized)) {
                return stringVariables.get(normalized);
            }
            if (builderVariables.containsKey(normalized)) {
                return builderVariables.get(normalized).toString();
            }
        }

        String quoted = extractQuotedSql(normalized);
        if (quoted != null) {
            return quoted;
        }

        return null;
    }

    private List<String> splitByPlus(String expression) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                current.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                current.append(c);
                inString = !inString;
                continue;
            }

            if (c == '+' && !inString) {
                parts.add(current.toString().trim());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            parts.add(current.toString().trim());
        }

        return parts;
    }

    private String extractFirstArgument(String statement) {
        int start = statement.indexOf('(');
        if (start < 0) {
            return null;
        }

        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        StringBuilder current = new StringBuilder();

        for (int i = start + 1; i < statement.length(); i++) {
            char c = statement.charAt(i);

            if (escaped) {
                current.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                current.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                current.append(c);
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    if (depth == 0) {
                        return current.toString().trim();
                    }
                    depth--;
                } else if (c == ',' && depth == 0) {
                    return current.toString().trim();
                }
            }

            current.append(c);
        }

        return current.toString().trim();
    }

    private boolean containsJdbcTemplateCall(String line) {
        return line.contains("jdbcTemplate.query(")
                || line.contains("jdbcTemplate.update(")
                || line.contains("jdbcTemplate.queryForObject(");
    }

    private String extractClassName(String line) {
        Matcher matcher = CLASS_PATTERN.matcher(line);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private boolean looksLikeMethodDeclaration(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("@")) {
            return false;
        }
        if (!trimmed.contains("(") || !trimmed.contains(")")) {
            return false;
        }
        if (!(trimmed.endsWith("{") || trimmed.endsWith(";"))) {
            return false;
        }

        String lower = trimmed.toLowerCase();
        if ((lower.startsWith("if ") || lower.startsWith("if("))
                || (lower.startsWith("for ") || lower.startsWith("for("))
                || (lower.startsWith("while ") || lower.startsWith("while("))
                || (lower.startsWith("switch ") || lower.startsWith("switch("))
                || (lower.startsWith("catch ") || lower.startsWith("catch("))
                || lower.startsWith("return ")
                || lower.startsWith("new ")) {
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

    private String extractQuotedSql(String line) {
        Matcher matcher = QUOTED_STRING_PATTERN.matcher(line);
        StringBuilder sqlBuilder = new StringBuilder();

        while (matcher.find()) {
            sqlBuilder.append(
                    matcher.group(1)
                            .replace("\\\"", "\"")
                            .replace("\\n", "\n")
                            .replace("\\t", "\t")
            );
        }

        if (sqlBuilder.length() == 0) {
            return null;
        }

        return sqlBuilder.toString();
    }

    private boolean isNativeQuery(String line) {
        String lower = line.toLowerCase();
        return lower.contains("nativequery = true") || lower.contains("nativequery=true");
    }

    private List<String> extractTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = TABLE_PATTERN.matcher(sql);
        while (matcher.find()) {
            tables.add(matcher.group(2).toLowerCase());
        }
        return new ArrayList<>(tables);
    }

    private List<String> extractNormalizedTokens(String sql) {
        Set<String> tokens = new LinkedHashSet<>();
        Matcher matcher = TOKEN_PATTERN.matcher(sql.toLowerCase());
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() < 2) {
                continue;
            }
            tokens.add(token);
            if (token.contains("_")) {
                String compact = token.replace("_", "");
                if (compact.length() >= 2) {
                    tokens.add(compact);
                }
            }
        }
        return new ArrayList<>(tokens);
    }

    private SqlAccessPoint buildAccessPoint(String className,
                                            String methodName,
                                            Path javaFile,
                                            String sql,
                                            SqlSourceType sourceType) {
        SqlAccessPoint accessPoint = new SqlAccessPoint();
        accessPoint.setSqlId(className + "#" + methodName + "#" + sourceType);
        accessPoint.setOwnerClassName(className);
        accessPoint.setOwnerMethodName(methodName);
        accessPoint.setSourceFile(javaFile.toString());
        accessPoint.setRawSql(sql);
        accessPoint.setSourceType(sourceType);
        accessPoint.setReferencedTables(extractTables(sql));
        accessPoint.setNormalizedTokens(extractNormalizedTokens(sql));
        return accessPoint;
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

    private static class PendingStringAssignment {
        private final String variableName;
        private final String expressionStart;

        private PendingStringAssignment(String variableName, String expressionStart) {
            this.variableName = variableName;
            this.expressionStart = expressionStart;
        }
    }
}