package com.schemascope.parser;

import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaColumn;
import com.schemascope.domain.SchemaTable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SchemaFileReader {

    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "(?is)\\bcreate\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?([`\"]?[a-zA-Z0-9_]+[`\"]?)\\s*\\("
    );

    public ParsedSchema read(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);

        List<SchemaTable> tables = new ArrayList<>();

        for (String statement : splitStatements(content)) {
            String normalized = stripInlineComments(statement).trim();
            if (normalized.isEmpty()) {
                continue;
            }

            Matcher matcher = CREATE_TABLE_PATTERN.matcher(normalized);
            if (!matcher.find()) {
                continue;
            }

            int openParen = normalized.indexOf('(', matcher.end() - 1);
            int closeParen = findMatchingParenthesis(normalized, openParen);
            if (openParen < 0 || closeParen < 0 || closeParen <= openParen) {
                continue;
            }

            SchemaTable table = new SchemaTable();
            table.setName(stripIdentifierQuotes(matcher.group(1)).toLowerCase());

            String definitionBlock = normalized.substring(openParen + 1, closeParen);
            for (String segment : splitTopLevelCommaSegments(definitionBlock)) {
                if (!looksLikeColumnDefinition(segment)) {
                    continue;
                }

                SchemaColumn column = extractColumn(segment);
                if (column != null) {
                    table.getColumns().add(column);
                }
            }

            tables.add(table);
        }

        return new ParsedSchema(tables);
    }

    private List<String> splitStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char next = i + 1 < content.length() ? content.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    current.append(c);
                }
                continue;
            }

            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick) {
                if (c == '-' && next == '-') {
                    inLineComment = true;
                    i++;
                    continue;
                }

                if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                    continue;
                }
            }

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
            }
            else if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            }
            else if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            }

            if (c == ';' && !inSingleQuote && !inDoubleQuote && !inBacktick) {
                String statement = current.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }

        return statements;
    }

    private String stripInlineComments(String statement) {
        StringBuilder cleaned = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inBacktick = false;

        for (int i = 0; i < statement.length(); i++) {
            char c = statement.charAt(i);
            char next = i + 1 < statement.length() ? statement.charAt(i + 1) : '\0';

            if (c == '\'' && !inDoubleQuote && !inBacktick) {
                inSingleQuote = !inSingleQuote;
            }
            else if (c == '"' && !inSingleQuote && !inBacktick) {
                inDoubleQuote = !inDoubleQuote;
            }
            else if (c == '`' && !inSingleQuote && !inDoubleQuote) {
                inBacktick = !inBacktick;
            }

            if (!inSingleQuote && !inDoubleQuote && !inBacktick && c == '-' && next == '-') {
                break;
            }

            cleaned.append(c);
        }

        return cleaned.toString();
    }

    private int findMatchingParenthesis(String text, int openParenIndex) {
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

    private List<String> splitTopLevelCommaSegments(String block) {
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

    private void addSegment(List<String> segments, StringBuilder current) {
        String segment = current.toString().trim();
        if (!segment.isEmpty()) {
            segments.add(segment);
        }
    }

    private boolean looksLikeColumnDefinition(String line) {
        String normalized = line.trim().toUpperCase();
        return !normalized.startsWith("PRIMARY KEY")
                && !normalized.startsWith("FOREIGN KEY")
                && !normalized.startsWith("UNIQUE")
                && !normalized.startsWith("CONSTRAINT")
                && !normalized.startsWith("INDEX")
                && !normalized.startsWith("KEY")
                && !normalized.startsWith("CHECK")
                && !normalized.startsWith("FULLTEXT")
                && !normalized.startsWith("SPATIAL");
    }

    private SchemaColumn extractColumn(String line) {
        String normalized = line.trim();
        if (normalized.endsWith(",")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }

        if (normalized.isEmpty()) {
            return null;
        }

        String[] parts = normalized.split("\\s+", 2);
        if (parts.length == 0) {
            return null;
        }

        String columnName = stripIdentifierQuotes(parts[0]).toLowerCase();
        if (columnName.isBlank()) {
            return null;
        }

        return new SchemaColumn(columnName, normalized);
    }

    private String stripIdentifierQuotes(String identifier) {
        if (identifier == null) {
            return "";
        }
        return identifier.replace("`", "").replace("\"", "");
    }
}