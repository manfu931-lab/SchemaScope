package com.schemascope.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaChangeTest {

    @Test
    void shouldCreateSchemaChangeObjectCorrectly() {
        SchemaChange change = new SchemaChange(
                "chg-001",
                ChangeType.DROP_COLUMN,
                "orders",
                "status",
                "varchar(16)",
                null,
                true,
                "V12__drop_orders_status.sql"
        );

        assertEquals("chg-001", change.getChangeId());
        assertEquals(ChangeType.DROP_COLUMN, change.getChangeType());
        assertEquals("orders", change.getTableName());
        assertEquals("status", change.getColumnName());
        assertEquals("varchar(16)", change.getOldType());
        assertEquals(null, change.getNewType());
        assertTrue(change.isBreaking());
        assertEquals("V12__drop_orders_status.sql", change.getSourceFile());
    }
}