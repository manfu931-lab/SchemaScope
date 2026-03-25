package com.schemascope.schemadiff;

import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaChange;
import com.schemascope.parser.SchemaFileReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaDiffServiceTest {

    @Test
    void shouldDetectDroppedColumnAndAlteredColumnType() throws Exception {
        SchemaFileReader reader = new SchemaFileReader();
        SchemaDiffService diffService = new SchemaDiffService();

        String oldPath = Paths.get("src", "test", "resources", "schema", "schema_v1.sql").toString();
        String newPath = Paths.get("src", "test", "resources", "schema", "schema_v2.sql").toString();

        ParsedSchema oldSchema = reader.read(oldPath);
        ParsedSchema newSchema = reader.read(newPath);

        List<SchemaChange> changes = diffService.diff(oldSchema, newSchema);

        assertEquals(2, changes.size());

        boolean foundDropColumn = changes.stream().anyMatch(change ->
                change.getChangeType().name().equals("DROP_COLUMN")
                        && change.getTableName().equals("orders")
                        && change.getColumnName().equals("status")
        );

        boolean foundAlterColumnType = changes.stream().anyMatch(change ->
                change.getChangeType().name().equals("ALTER_COLUMN_TYPE")
                        && change.getTableName().equals("users")
                        && change.getColumnName().equals("email")
        );

        assertTrue(foundDropColumn);
        assertTrue(foundAlterColumnType);
        System.out.println(changes);
    }
}