package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ChangeType;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

@Component
public class SchemaChangeFactory {

    public SchemaChange fromRequest(AnalysisRequest request) {
        ChangeType type = ChangeType.valueOf(request.getChangeType());
        boolean breaking = isBreakingChange(type);

        return new SchemaChange(
                buildChangeId(type, request.getTableName(), request.getColumnName()),
                type,
                request.getTableName(),
                request.getColumnName(),
                request.getOldType(),
                request.getNewType(),
                breaking,
                request.getSourceFile()
        );
    }

    private boolean isBreakingChange(ChangeType type) {
        return type == ChangeType.DROP_COLUMN
                || type == ChangeType.DROP_TABLE
                || type == ChangeType.ALTER_COLUMN_TYPE
                || type == ChangeType.RENAME_COLUMN;
    }

    private String buildChangeId(ChangeType type, String tableName, String columnName) {
        String safeType = safe(type.name());
        String safeTable = safe(tableName);
        String safeColumn = safe(columnName);
        return "chg-" + safeType + "-" + safeTable + "-" + safeColumn;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}