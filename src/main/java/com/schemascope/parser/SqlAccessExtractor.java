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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class SqlAccessExtractor {

    private static final Pattern TYPE_DECLARATION_PATTERN =
            Pattern.compile("^(?:public|protected|private|abstract|final|sealed|non-sealed|static\\s+)*" +
                    "(class|interface|record|enum)\\s+([A-Za-z_][A-Za-z0-9_]*)\\b");

    private static final Pattern METHOD_DECLARATION_PATTERN = Pattern.compile(
            "^(?:public|protected|private|static|final|default|synchronized|abstract|native|strictfp|\\s)*" +
                    "[A-Za-z0-9_<>\\[\\],.? ]+\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\([^)]*\\)\\s*(?:\\{|;)$"
    );

    private static final Pattern QUOTED_STRING_PATTERN =
            Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"");

    private static final Pattern STRING_VARIABLE_DECLARATION_PATTERN = Pattern.compile(
            "^(?:final\\s+)?String\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.*)$"
    );

    private static final Pattern STRING_VARIABLE_APPEND_PATTERN = Pattern.compile(
            "^([A-Za-z_][A-Za-z0-9_]*)\\s*\\+=\\s*(.*)$"
    );

    private static final Pattern STRING_BUILDER_DECLARATION_PATTERN = Pattern.compile(
            "^(?:final\\s+)?StringBuilder\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*new\\s+StringBuilder\\s*\\((.*)$"
    );

    private static final Pattern STRING_BUILDER_APPEND_PATTERN = Pattern.compile(
            "^([A-Za-z_][A-Za-z0-9_]*)\\.append\\s*\\((.*)$"
    );

    private static final Pattern TABLE_PATTERN =
            Pattern.compile("(?i)\\b(from|join|update|into)\\s+([`\"]?[a-zA-Z0-9_]+[`\"]?)");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-z0-9_]+");

    private static final Pattern TABLE_ANNOTATION_PATTERN = Pattern.compile(
            "@(?:jakarta\\.persistence\\.|javax\\.persistence\\.)?Table\\s*\\(.*?name\\s*=\\s*\"([^\"]+)\".*?\\)",
            Pattern.DOTALL
    );

    private static final Pattern ENTITY_ANNOTATION_PATTERN = Pattern.compile(
            "@(?:jakarta\\.persistence\\.|javax\\.persistence\\.)?Entity\\b"
    );

    private static final Pattern REPOSITORY_ENTITY_PATTERN = Pattern.compile(
            "(?:extends|implements|,)\\s*[A-Za-z0-9_$.]*" +
                    "(?:Repository|CrudRepository|ListCrudRepository|PagingAndSortingRepository|" +
                    "ListPagingAndSortingRepository|JpaRepository|JpaSpecificationExecutor|" +
                    "MongoRepository|ElasticsearchRepository|ReactiveCrudRepository|R2dbcRepository)\\s*<\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*,",
            Pattern.CASE_INSENSITIVE
    );

    private static final List<String> DERIVED_QUERY_PREFIXES = List.of(
            "find", "read", "get", "query", "search", "stream", "count", "exists", "delete", "remove"
    );

    private static final List<String> DERIVED_QUERY_SUFFIXES = List.of(
            "IsNotNull",
            "NotNull",
            "StartingWith",
            "EndingWith",
            "Containing",
            "Contains",
            "GreaterThanEqual",
            "LessThanEqual",
            "GreaterThan",
            "LessThan",
            "NotLike",
            "IgnoreCase",
            "AllIgnoreCase",
            "IsNull",
            "Between",
            "Before",
            "After",
            "NotIn",
            "False",
            "True",
            "Like",
            "Null",
            "Not",
            "In",
            "Is",
            "Equals"
    );

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

        Path sourceRoot = projectRoot.resolve(Path.of("src", "main", "java"));
        if (!Files.exists(sourceRoot)) {
            sourceRoot = projectRoot;
        }

        if (!Files.exists(sourceRoot)) {
            throw new IllegalArgumentException("Source root does not exist: " + sourceRoot);
        }

        List<Path> javaFiles;
        try (Stream<Path> pathStream = Files.walk(sourceRoot)) {
            javaFiles = pathStream
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .toList();
        }

        Map<String, String> entityTableIndex = buildEntityTableIndex(javaFiles);

        for (Path javaFile : javaFiles) {
            results.addAll(extractFromJavaFile(javaFile, entityTableIndex));
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

    List<SqlAccessPoint> extractFromJavaFile(Path javaFile) throws IOException {
        return extractFromJavaFile(javaFile, Map.of());
    }

    List<SqlAccessPoint> extractFromJavaFile(Path javaFile,
                                             Map<String, String> entityTableIndex) throws IOException {
        List<SqlAccessPoint> results = new ArrayList<>();

        String content = Files.readString(javaFile, StandardCharsets.UTF_8);
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        String currentClassName = extractPrimaryClassName(content, javaFile);

        String repositoryEntityType = extractRepositoryEntityType(content);
        if ((repositoryEntityType == null || repositoryEntityType.isBlank())
                && looksLikeRepositoryClass(currentClassName)) {
            repositoryEntityType = inferEntityTypeFromClassName(currentClassName);
        }

        boolean repositoryLike = repositoryEntityType != null && !repositoryEntityType.isBlank();
        String repositoryTableName = resolveRepositoryTableName(repositoryEntityType, entityTableIndex);

        String currentMethodName = null;
        String pendingQuerySql = null;
        SqlSourceType pendingQueryType = null;
        StringBuilder queryAnnotationBuffer = null;

        Map<String, String> localSqlVariables = new LinkedHashMap<>();
        Map<String, StringBuilder> localSqlBuilders = new LinkedHashMap<>();

        String pendingSqlVariableName = null;
        StringBuilder pendingSqlVariableExpression = null;
        boolean pendingSqlVariableAppend = false;

        String pendingBuilderVariableName = null;
        StringBuilder pendingBuilderExpression = null;
        boolean pendingBuilderAppend = false;

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

            if (pendingSqlVariableName != null) {
                pendingSqlVariableExpression.append(' ').append(line);
                if (line.endsWith(";")) {
                    storeSqlVariable(
                            localSqlVariables,
                            pendingSqlVariableName,
                            extractQuotedSql(pendingSqlVariableExpression.toString()),
                            pendingSqlVariableAppend
                    );
                    pendingSqlVariableName = null;
                    pendingSqlVariableExpression = null;
                    pendingSqlVariableAppend = false;
                }
                continue;
            }

            if (pendingBuilderVariableName != null) {
                pendingBuilderExpression.append(' ').append(line);
                if (line.endsWith(";")) {
                    storeStringBuilderFragment(
                            localSqlBuilders,
                            pendingBuilderVariableName,
                            extractQuotedSql(pendingBuilderExpression.toString()),
                            pendingBuilderAppend
                    );
                    pendingBuilderVariableName = null;
                    pendingBuilderExpression = null;
                    pendingBuilderAppend = false;
                }
                continue;
            }

            if (startsQueryAnnotation(line)) {
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
                localSqlVariables.clear();
                localSqlBuilders.clear();

                pendingSqlVariableName = null;
                pendingSqlVariableExpression = null;
                pendingSqlVariableAppend = false;

                pendingBuilderVariableName = null;
                pendingBuilderExpression = null;
                pendingBuilderAppend = false;

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
                    continue;
                }

                if (repositoryLike && currentMethodName != null && isDerivedQueryMethod(currentMethodName)) {
                    SqlAccessPoint derivedAccessPoint = buildDerivedQueryAccessPoint(
                            currentClassName,
                            currentMethodName,
                            javaFile,
                            repositoryEntityType,
                            repositoryTableName
                    );

                    if (derivedAccessPoint != null) {
                        results.add(derivedAccessPoint);
                    }
                }
            }

            String declaredVariableName = extractDeclaredSqlVariableName(line);
            if (declaredVariableName != null) {
                if (line.endsWith(";")) {
                    storeSqlVariable(
                            localSqlVariables,
                            declaredVariableName,
                            extractQuotedSql(line),
                            false
                    );
                } else {
                    pendingSqlVariableName = declaredVariableName;
                    pendingSqlVariableExpression = new StringBuilder(line);
                    pendingSqlVariableAppend = false;
                }
                continue;
            }

            String appendedVariableName = extractAppendedSqlVariableName(line);
            if (appendedVariableName != null) {
                if (line.endsWith(";")) {
                    storeSqlVariable(
                            localSqlVariables,
                            appendedVariableName,
                            extractQuotedSql(line),
                            true
                    );
                } else {
                    pendingSqlVariableName = appendedVariableName;
                    pendingSqlVariableExpression = new StringBuilder(line);
                    pendingSqlVariableAppend = true;
                }
                continue;
            }

            String declaredBuilderName = extractDeclaredStringBuilderName(line);
            if (declaredBuilderName != null) {
                if (line.endsWith(";")) {
                    storeStringBuilderFragment(
                            localSqlBuilders,
                            declaredBuilderName,
                            extractQuotedSql(line),
                            false
                    );
                } else {
                    pendingBuilderVariableName = declaredBuilderName;
                    pendingBuilderExpression = new StringBuilder(line);
                    pendingBuilderAppend = false;
                }
                continue;
            }

            String appendedBuilderName = extractAppendedStringBuilderName(line);
            if (appendedBuilderName != null) {
                if (line.endsWith(";")) {
                    storeStringBuilderFragment(
                            localSqlBuilders,
                            appendedBuilderName,
                            extractQuotedSql(line),
                            true
                    );
                } else {
                    pendingBuilderVariableName = appendedBuilderName;
                    pendingBuilderExpression = new StringBuilder(line);
                    pendingBuilderAppend = true;
                }
                continue;
            }

            if (containsJdbcTemplateCall(line)) {
                String sql = extractJdbcTemplateSql(line, localSqlVariables, localSqlBuilders);
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

    private List<SqlAccessPoint> extractFromXmlFile(Path xmlFile)
            throws IOException, ParserConfigurationException, SAXException {
        return myBatisXmlSqlExtractor.extractFromXmlFile(xmlFile);
    }

    private Map<String, String> buildEntityTableIndex(List<Path> javaFiles) throws IOException {
        Map<String, String> entityTableIndex = new LinkedHashMap<>();

        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile, StandardCharsets.UTF_8);
            if (!ENTITY_ANNOTATION_PATTERN.matcher(content).find()) {
                continue;
            }

            String entityClassName = extractPrimaryClassName(content, javaFile);
            if (entityClassName == null || entityClassName.isBlank()) {
                continue;
            }

            Matcher tableMatcher = TABLE_ANNOTATION_PATTERN.matcher(content);
            String tableName;
            if (tableMatcher.find()) {
                tableName = tableMatcher.group(1).trim().toLowerCase();
            } else {
                tableName = pluralize(toSnakeCase(entityClassName));
            }

            entityTableIndex.put(entityClassName, tableName);
        }

        return entityTableIndex;
    }

    private boolean startsQueryAnnotation(String line) {
        return line.startsWith("@Query")
                || line.startsWith("@org.springframework.data.jpa.repository.Query");
    }

    private boolean containsJdbcTemplateCall(String line) {
        return line.contains("jdbcTemplate.query(")
                || line.contains("jdbcTemplate.update(")
                || line.contains("jdbcTemplate.queryForObject(");
    }

    private String extractPrimaryClassName(String content, Path javaFile) {
        String[] lines = content.split("\\R");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()
                    || line.startsWith("package ")
                    || line.startsWith("import ")
                    || line.startsWith("//")
                    || line.startsWith("/*")
                    || line.startsWith("*")
                    || line.startsWith("*/")
                    || line.startsWith("@")) {
                continue;
            }

            Matcher matcher = TYPE_DECLARATION_PATTERN.matcher(line);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }

        String fileName = javaFile.getFileName().toString();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }

    private String extractRepositoryEntityType(String content) {
        String normalized = content
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        Matcher matcher = REPOSITORY_ENTITY_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean looksLikeRepositoryClass(String className) {
        if (className == null || className.isBlank()) {
            return false;
        }

        return className.endsWith("Repository")
                || className.endsWith("Dao")
                || className.endsWith("Mapper");
    }

    private String inferEntityTypeFromClassName(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }

        if (className.endsWith("Repository")) {
            return className.substring(0, className.length() - "Repository".length());
        }

        if (className.endsWith("Dao")) {
            return className.substring(0, className.length() - "Dao".length());
        }

        if (className.endsWith("Mapper")) {
            return className.substring(0, className.length() - "Mapper".length());
        }

        return null;
    }

    private String resolveRepositoryTableName(String repositoryEntityType,
                                              Map<String, String> entityTableIndex) {
        if (repositoryEntityType == null || repositoryEntityType.isBlank()) {
            return null;
        }

        String indexed = entityTableIndex.get(repositoryEntityType);
        if (indexed != null && !indexed.isBlank()) {
            return indexed;
        }

        return pluralize(toSnakeCase(repositoryEntityType));
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

    private String extractDeclaredSqlVariableName(String line) {
        Matcher matcher = STRING_VARIABLE_DECLARATION_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractAppendedSqlVariableName(String line) {
        Matcher matcher = STRING_VARIABLE_APPEND_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private void storeSqlVariable(Map<String, String> localSqlVariables,
                                  String variableName,
                                  String sqlFragment,
                                  boolean append) {
        if (variableName == null || variableName.isBlank() || sqlFragment == null || sqlFragment.isBlank()) {
            return;
        }

        if (append) {
            localSqlVariables.merge(variableName, sqlFragment, String::concat);
        } else {
            localSqlVariables.put(variableName, sqlFragment);
        }
    }

    private String extractDeclaredStringBuilderName(String line) {
        Matcher matcher = STRING_BUILDER_DECLARATION_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractAppendedStringBuilderName(String line) {
        Matcher matcher = STRING_BUILDER_APPEND_PATTERN.matcher(line.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private void storeStringBuilderFragment(Map<String, StringBuilder> localSqlBuilders,
                                            String variableName,
                                            String sqlFragment,
                                            boolean append) {
        if (variableName == null || variableName.isBlank()) {
            return;
        }

        if (!append) {
            localSqlBuilders.put(variableName, new StringBuilder());
        }

        StringBuilder builder = localSqlBuilders.computeIfAbsent(variableName, key -> new StringBuilder());
        if (sqlFragment != null && !sqlFragment.isBlank()) {
            builder.append(sqlFragment);
        }
    }

    private String extractJdbcTemplateSql(String line,
                                          Map<String, String> localSqlVariables,
                                          Map<String, StringBuilder> localSqlBuilders) {
        String directSql = extractQuotedSql(line);
        if (directSql != null) {
            return directSql;
        }

        String firstArgument = extractFirstMethodArgument(line);
        if (firstArgument == null || firstArgument.isBlank()) {
            return null;
        }

        String normalizedArgument = firstArgument.trim();

        String variableSql = localSqlVariables.get(normalizedArgument);
        if (variableSql != null) {
            return variableSql;
        }

        String builderVariableName = extractStringBuilderReferenceName(normalizedArgument);
        if (builderVariableName != null) {
            StringBuilder builder = localSqlBuilders.get(builderVariableName);
            if (builder != null && builder.length() > 0) {
                return builder.toString();
            }
        }

        return null;
    }

    private String extractFirstMethodArgument(String line) {
        int openParen = line.indexOf('(');
        if (openParen < 0) {
            return null;
        }

        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (int i = openParen + 1; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '(') {
                depth++;
            } else if (c == ')') {
                if (depth == 0) {
                    break;
                }
                depth--;
            } else if (c == ',' && depth == 0) {
                break;
            }

            current.append(c);
        }

        String argument = current.toString().trim();
        if (argument.endsWith(";")) {
            argument = argument.substring(0, argument.length() - 1).trim();
        }

        return argument;
    }

    private String extractStringBuilderReferenceName(String argument) {
        if (argument == null || argument.isBlank()) {
            return null;
        }

        String trimmed = argument.trim();
        if (trimmed.endsWith(".toString()")) {
            return trimmed.substring(0, trimmed.length() - ".toString()".length()).trim();
        }

        return trimmed;
    }

    private boolean isNativeQuery(String line) {
        String lower = line.toLowerCase();
        return lower.contains("nativequery = true") || lower.contains("nativequery=true");
    }

    private boolean isDerivedQueryMethod(String methodName) {
        String lower = methodName.toLowerCase();
        boolean hasKnownPrefix = DERIVED_QUERY_PREFIXES.stream().anyMatch(lower::startsWith);
        if (!hasKnownPrefix) {
            return false;
        }

        return methodName.contains("By");
    }

    private SqlAccessPoint buildDerivedQueryAccessPoint(String className,
                                                        String methodName,
                                                        Path javaFile,
                                                        String repositoryEntityType,
                                                        String repositoryTableName) {
        List<String> properties = extractDerivedQueryProperties(methodName);
        if (properties.isEmpty()) {
            return null;
        }

        String effectiveTable = repositoryTableName;
        if (effectiveTable == null || effectiveTable.isBlank()) {
            if (repositoryEntityType == null || repositoryEntityType.isBlank()) {
                return null;
            }
            effectiveTable = pluralize(toSnakeCase(repositoryEntityType));
        }

        List<String> predicates = new ArrayList<>();
        for (String property : properties) {
            predicates.add(property + " = ?");
        }

        String pseudoSql = "select * from " + effectiveTable
                + " where " + String.join(" and ", predicates)
                + " /* derived query: " + methodName + " */";

        return buildAccessPoint(
                className,
                methodName,
                javaFile,
                pseudoSql,
                SqlSourceType.JPA_QUERY
        );
    }

    private List<String> extractDerivedQueryProperties(String methodName) {
        int byIndex = methodName.indexOf("By");
        if (byIndex < 0 || byIndex + 2 >= methodName.length()) {
            return List.of();
        }

        String criteriaPart = methodName.substring(byIndex + 2);
        int orderByIndex = criteriaPart.indexOf("OrderBy");
        if (orderByIndex >= 0) {
            criteriaPart = criteriaPart.substring(0, orderByIndex);
        }

        if (criteriaPart.isBlank()) {
            return List.of();
        }

        List<String> properties = new ArrayList<>();
        for (String segment : splitDerivedCriteriaSegments(criteriaPart)) {
            String property = stripDerivedQuerySuffixes(segment);
            if (property.isBlank()) {
                continue;
            }

            String token = toSnakeCase(property);
            if (!token.isBlank()) {
                properties.add(token);
            }
        }

        return properties;
    }

    private List<String> splitDerivedCriteriaSegments(String criteriaPart) {
        String normalized = criteriaPart.replaceAll("(And|Or)(?=[A-Z])", "|");
        String[] parts = normalized.split("\\|");

        List<String> segments = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                segments.add(trimmed);
            }
        }
        return segments;
    }

    private String stripDerivedQuerySuffixes(String propertySegment) {
        String current = propertySegment;
        boolean changed = true;

        while (changed) {
            changed = false;
            for (String suffix : DERIVED_QUERY_SUFFIXES) {
                if (current.endsWith(suffix) && current.length() > suffix.length()) {
                    current = current.substring(0, current.length() - suffix.length());
                    changed = true;
                    break;
                }
            }
        }

        return current;
    }

    private List<String> extractTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = TABLE_PATTERN.matcher(sql);
        while (matcher.find()) {
            String normalized = stripIdentifierQuotes(matcher.group(2)).toLowerCase();
            if (!normalized.isBlank()) {
                tables.add(normalized);
            }
        }
        return new ArrayList<>(tables);
    }

    private String stripIdentifierQuotes(String identifier) {
        if (identifier == null) {
            return "";
        }
        return identifier.replace("`", "").replace("\"", "").trim();
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

    private String toSnakeCase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('.', '_')
                .toLowerCase();
    }

    private String pluralize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.endsWith("s")) {
            return value;
        }

        if (value.endsWith("y") && value.length() > 1) {
            return value.substring(0, value.length() - 1) + "ies";
        }

        return value + "s";
    }
}