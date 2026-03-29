package com.schemascope.parser;

import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaTable;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldReadCompactPetClinicStyleSchema() throws Exception {
        SchemaFileReader reader = new SchemaFileReader();

        Path tempSchema = Files.createTempFile("petclinic-compact", ".sql");
        Files.writeString(
                tempSchema,
                "CREATE TABLE IF NOT EXISTS owners ( id INT (4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, first_name VARCHAR (30), last_name VARCHAR (30), address VARCHAR (255), city VARCHAR (80), telephone VARCHAR (20), INDEX (last_name) ) engine=InnoDB; " +
                        "CREATE TABLE IF NOT EXISTS pets ( id INT (4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, owner_id INT (4) UNSIGNED );",
                StandardCharsets.UTF_8
        );
        tempSchema.toFile().deleteOnExit();

        ParsedSchema parsedSchema = reader.read(tempSchema.toString());

        assertEquals(2, parsedSchema.getTables().size());
        assertEquals("owners", parsedSchema.getTables().get(0).getName());

        assertTrue(parsedSchema.getTables().get(0).getColumns().stream()
                .anyMatch(column -> "last_name".equals(column.getName())));

        assertTrue(parsedSchema.getTables().get(0).getColumns().stream()
                .noneMatch(column -> "index".equals(column.getName())));
    }
}