package com.schemascope.parser;

import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaTable;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaFileReaderTest {

    @Test
    void shouldReadSchemaFileAndExtractTablesAndColumns() throws Exception {
        SchemaFileReader reader = new SchemaFileReader();

        String filePath = Paths.get("src", "test", "resources", "schema", "schema_v1.sql").toString();
        ParsedSchema parsedSchema = reader.read(filePath);

        assertEquals(2, parsedSchema.getTables().size());

        SchemaTable usersTable = parsedSchema.getTables().get(0);
        assertEquals("users", usersTable.getName());
        assertEquals(3, usersTable.getColumns().size());
        assertEquals("id", usersTable.getColumns().get(0).getName());
        assertEquals("email", usersTable.getColumns().get(1).getName());

        SchemaTable ordersTable = parsedSchema.getTables().get(1);
        assertEquals("orders", ordersTable.getName());
        assertEquals(3, ordersTable.getColumns().size());
        assertEquals("status", ordersTable.getColumns().get(1).getName());
        System.out.println(parsedSchema);
    }
}