package com.schemascope.parser;

import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlSourceType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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
            Pattern.compile("(?i)\\b(from|join|update|into)\\s+([a-zA-Z0-9_]+)");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-z0-9_]+");

    public List<SqlAccessPoint> extractFromProject(String projectRootPath) throws IOException {
        List<SqlAccessPoint> results = new ArrayList<>();

        Path projectRoot = Path.of(projectRootPath).toAbsolutePath().normalize();
        if (!Files.exists(projectRoot)) {
            throw new IllegalArgumentException("Project root does not exist: " + projectRoot);
        }

        Path sourceRoot = projectRoot.resolve(Path.of("src", "main", "java"));
        if (!Files.exists(sourceRoot)) {
            sourceRoot = projectRoot;
        }

        if (!Files.exists(sourceRoot)) {
            throw new IllegalArgumentException("Source root does not exist: " + sourceRoot);
        }

        try (Stream<Path> pathStream = Files.walk(sourceRoot)) {
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

        return results;
    }

    List<SqlAccessPoint> extractFromJavaFile(Path javaFile) throws IOException {
        List<SqlAccessPoint> results = new ArrayList<>();
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        String currentClassName = null;
        String currentMethodName = null;
        String pendingQuerySql = null;
        SqlSourceType pendingQueryType = null;
        StringBuilder queryAnnotationBuffer = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

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
                continue;
            }

            if (looksLikeMethodDeclaration(line)) {
                currentMethodName = extractMethodName(line);

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
            }

            if (containsJdbcTemplateCall(line)) {
                String sql = extractQuotedSql(line);
                if (sql != null && currentMethodName != null) {
                    results.add(buildAccessPoint(
                            currentClassName,
                            currentMethodName,
                            javaFile,
                            sql,
                            SqlSourceType.JDBC_TEMPLATE
                    ));
                }
            }
        }

        return results;
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
}