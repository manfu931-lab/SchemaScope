package com.schemascope.parser;

import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaColumn;
import com.schemascope.domain.SchemaTable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class SchemaFileReader {

    public ParsedSchema read(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));

        List<SchemaTable> tables = new ArrayList<>();

        SchemaTable currentTable = null;
        boolean insideCreateTable = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }

            if (line.toUpperCase().startsWith("CREATE TABLE")) {
                String tableName = extractTableName(line);
                currentTable = new SchemaTable();
                currentTable.setName(tableName);
                tables.add(currentTable);
                insideCreateTable = true;
                continue;
            }

            if (insideCreateTable) {
                if (line.startsWith(");") || line.equals(")")) {
                    insideCreateTable = false;
                    currentTable = null;
                    continue;
                }

                if (currentTable != null && looksLikeColumnDefinition(line)) {
                    SchemaColumn column = extractColumn(line);
                    if (column != null) {
                        currentTable.getColumns().add(column);
                    }
                }
            }
        }

        return new ParsedSchema(tables);
    }

    private String extractTableName(String line) {
        String normalized = line.replace("(", " ").trim();
        String[] parts = normalized.split("\\s+");
        return parts[2];
    }

    private boolean looksLikeColumnDefinition(String line) {
        String upper = line.toUpperCase();
        return !upper.startsWith("PRIMARY KEY")
                && !upper.startsWith("FOREIGN KEY")
                && !upper.startsWith("UNIQUE")
                && !upper.startsWith("CONSTRAINT");
    }

    private SchemaColumn extractColumn(String line) {
        String cleaned = line;
        if (cleaned.endsWith(",")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        String[] parts = cleaned.split("\\s+", 2);
        if (parts.length < 2) {
            return null;
        }

        String columnName = parts[0];
        return new SchemaColumn(columnName, cleaned);
    }
}