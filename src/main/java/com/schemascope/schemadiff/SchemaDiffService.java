package com.schemascope.schemadiff;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SchemaColumn;
import com.schemascope.domain.SchemaTable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SchemaDiffService {

    public List<SchemaChange> diff(ParsedSchema oldSchema, ParsedSchema newSchema) {
        List<SchemaChange> changes = new ArrayList<>();

        for (SchemaTable oldTable : oldSchema.getTables()) {
            Optional<SchemaTable> matchedNewTableOpt = findTableByName(newSchema, oldTable.getName());

            if (matchedNewTableOpt.isEmpty()) {
                continue;
            }

            SchemaTable newTable = matchedNewTableOpt.get();

            for (SchemaColumn oldColumn : oldTable.getColumns()) {
                Optional<SchemaColumn> matchedNewColumnOpt = findColumnByName(newTable, oldColumn.getName());

                if (matchedNewColumnOpt.isEmpty()) {
                    changes.add(new SchemaChange(
                            buildChangeId(ChangeType.DROP_COLUMN, oldTable.getName(), oldColumn.getName()),
                            ChangeType.DROP_COLUMN,
                            oldTable.getName(),
                            oldColumn.getName(),
                            oldColumn.getDefinition(),
                            null,
                            true,
                            "schema-diff"
                    ));
                    continue;
                }

                SchemaColumn newColumn = matchedNewColumnOpt.get();

                if (!oldColumn.getDefinition().equalsIgnoreCase(newColumn.getDefinition())) {
                    changes.add(new SchemaChange(
                            buildChangeId(ChangeType.ALTER_COLUMN_TYPE, oldTable.getName(), oldColumn.getName()),
                            ChangeType.ALTER_COLUMN_TYPE,
                            oldTable.getName(),
                            oldColumn.getName(),
                            oldColumn.getDefinition(),
                            newColumn.getDefinition(),
                            true,
                            "schema-diff"
                    ));
                }
            }
        }

        return changes;
    }

    private Optional<SchemaTable> findTableByName(ParsedSchema schema, String tableName) {
        return schema.getTables()
                .stream()
                .filter(table -> table.getName().equalsIgnoreCase(tableName))
                .findFirst();
    }

    private Optional<SchemaColumn> findColumnByName(SchemaTable table, String columnName) {
        return table.getColumns()
                .stream()
                .filter(column -> column.getName().equalsIgnoreCase(columnName))
                .findFirst();
    }

    private String buildChangeId(ChangeType type, String tableName, String columnName) {
        return "chg-" + type.name().toLowerCase().replace("_", "-")
                + "-" + tableName.toLowerCase()
                + "-" + columnName.toLowerCase();
    }
}