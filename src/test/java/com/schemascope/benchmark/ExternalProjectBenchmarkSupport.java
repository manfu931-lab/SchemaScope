package com.schemascope.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExternalProjectBenchmarkSupport {

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?is)\\bcreate\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?([`\"]?[a-zA-Z0-9_]+[`\"]?)\\s*\\("
    );

    private ExternalProjectBenchmarkSupport() {
    }

    public static Path requireProjectRoot(String systemPropertyName, String environmentVariableName) {
        String propertyValue = System.getProperty(systemPropertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            Path path = Path.of(propertyValue).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                return path;
            }
            throw new IllegalArgumentException("External project path does not exist: " + path);
        }

        String envValue = System.getenv(environmentVariableName);
        if (envValue != null && !envValue.isBlank()) {
            Path path = Path.of(envValue).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                return path;
            }
            throw new IllegalArgumentException("External project path does not exist: " + path);
        }

        throw new IllegalStateException(
                "Missing external project path. Set -D" + systemPropertyName
                        + "=<projectRoot> or env " + environmentVariableName + "=<projectRoot>"
        );
    }

    public static Path resolvePetClinicSchema(Path projectRoot) {
        Path h2Schema = projectRoot.resolve(Path.of("src", "main", "resources", "db", "h2", "schema.sql"));
        if (Files.exists(h2Schema)) {
            return h2Schema;
        }

        Path mysqlSchema = projectRoot.resolve(Path.of("src", "main", "resources", "db", "mysql", "schema.sql"));
        if (Files.exists(mysqlSchema)) {
            return mysqlSchema;
        }

        throw new IllegalStateException(
                "Could not locate PetClinic schema.sql under db/h2 or db/mysql in " + projectRoot
        );
    }

    public static Path createDroppedColumnVariant(Path originalSchema,
                                                  String tableName,
                                                  String columnName) throws IOException {
        String original = Files.readString(originalSchema, StandardCharsets.UTF_8);
        String withoutColumn = removeColumnFromCreateTable(original, tableName, columnName);

        if (original.equals(withoutColumn)) {
            throw new IllegalStateException(
                    "Failed to remove column '" + columnName + "' from table '" + tableName + "' in " + originalSchema
            );
        }

        Path tempFile = Files.createTempFile("schemascope-external-", "-schema-v2.sql");
        Files.writeString(tempFile, withoutColumn, StandardCharsets.UTF_8);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    public static BenchmarkCase buildManualDropColumnCase(Path projectRoot) {
        return new BenchmarkCase(
                "spring-petclinic-manual-drop-column",
                "External PetClinic manual request should surface owner repository/controller impact",
                new com.schemascope.domain.AnalysisRequest(
                        "spring-petclinic",
                        projectRoot.toString(),
                        null,
                        null,
                        "DROP_COLUMN",
                        "owners",
                        "last_name",
                        "VARCHAR(30)",
                        null,
                        "external-benchmark"
                ),
                BenchmarkProjectSpec.linkedSet(
                        "OwnerRepository",
                        "OwnerController"
                ),
                BenchmarkProjectSpec.linkedSet("OwnerRepository"),
                BenchmarkProjectSpec.linkedSet("OwnerController"),
                true
        );
    }

    public static BenchmarkCase buildSchemaDiffDropColumnCase(Path projectRoot,
                                                              Path oldSchema,
                                                              Path newSchema) {
        return new BenchmarkCase(
                "spring-petclinic-schema-diff-drop-column",
                "External PetClinic schema diff should surface owner repository/controller impact",
                new com.schemascope.domain.AnalysisRequest(
                        "spring-petclinic",
                        projectRoot.toString(),
                        oldSchema.toString(),
                        newSchema.toString(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                BenchmarkProjectSpec.linkedSet(
                        "OwnerRepository",
                        "OwnerController"
                ),
                BenchmarkProjectSpec.linkedSet("OwnerRepository"),
                BenchmarkProjectSpec.linkedSet("OwnerController"),
                true
        );
    }

    private static String removeColumnFromCreateTable(String content, String tableName, String columnName) {
        Matcher matcher = CREATE_TABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String matchedTable = stripIdentifierQuotes(matcher.group(1));
            if (!matchedTable.equalsIgnoreCase(tableName)) {
                continue;
            }

            int openParen = content.indexOf('(', matcher.end() - 1);
            int closeParen = findMatchingParenthesis(content, openParen);
            if (openParen < 0 || closeParen < 0 || closeParen <= openParen) {
                return content;
            }

            List<String> segments = splitTopLevelCommaSegments(content.substring(openParen + 1, closeParen));
            List<String> filtered = new ArrayList<>();
            boolean removed = false;

            for (String segment : segments) {
                if (isTargetColumnDefinition(segment, columnName)) {
                    removed = true;
                    continue;
                }
                filtered.add(segment.trim());
            }

            if (!removed) {
                return content;
            }

            return content.substring(0, openParen + 1)
                    + String.join(", ", filtered)
                    + content.substring(closeParen);
        }

        return content;
    }

    private static boolean isTargetColumnDefinition(String segment, String columnName) {
        String normalized = segment.trim();
        if (normalized.isEmpty()) {
            return false;
        }

        String upper = normalized.toUpperCase();
        if (upper.startsWith("PRIMARY KEY")
                || upper.startsWith("FOREIGN KEY")
                || upper.startsWith("UNIQUE")
                || upper.startsWith("CONSTRAINT")
                || upper.startsWith("INDEX")
                || upper.startsWith("KEY")
                || upper.startsWith("CHECK")) {
            return false;
        }

        String[] parts = normalized.split("\\s+", 2);
        if (parts.length == 0) {
            return false;
        }

        return stripIdentifierQuotes(parts[0]).equalsIgnoreCase(columnName);
    }

    private static List<String> splitTopLevelCommaSegments(String block) {
        List<String> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;

        for (int i = 0; i < block.length(); i++) {
            char c = block.charAt(i);

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
            }
            else if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            }
            else if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '(') {
                    depth++;
                }
                else if (c == ')') {
                    depth--;
                }
                else if (c == ',' && depth == 0) {
                    addSegment(segments, current);
                    current.setLength(0);
                    continue;
                }
            }

            current.append(c);
        }

        addSegment(segments, current);
        return segments;
    }

    private static void addSegment(List<String> segments, StringBuilder current) {
        String segment = current.toString().trim();
        if (!segment.isEmpty()) {
            segments.add(segment);
        }
    }

    private static int findMatchingParenthesis(String text, int openParenIndex) {
        if (openParenIndex < 0 || openParenIndex >= text.length() || text.charAt(openParenIndex) != '(') {
            return -1;
        }

        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;

        for (int i = openParenIndex; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
            }
            else if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            }
            else if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            }

            if (inSingleQuote || inDoubleQuote || inBacktick) {
                continue;
            }

            if (c == '(') {
                depth++;
            }
            else if (c == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static String stripIdentifierQuotes(String identifier) {
        if (identifier == null) {
            return "";
        }
        return identifier.replace("`", "").replace("\"", "");
    }
}