package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ChangeType;
import com.schemascope.domain.SchemaChange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaChangeFactoryTest {

    @Test
    void shouldBuildSchemaChangeFromRequest() {
        SchemaChangeFactory factory = new SchemaChangeFactory();

        AnalysisRequest request = new AnalysisRequest(
                "petclinic",
                "/benchmark/petclinic",
                "schema_v1.sql",
                "schema_v2.sql",
                "DROP_COLUMN",
                "orders",
                "status",
                "varchar(16)",
                null,
                "V12__drop_orders_status.sql"
        );

        SchemaChange change = factory.fromRequest(request);

        assertEquals("chg-drop-column-orders-status", change.getChangeId());
        assertEquals(ChangeType.DROP_COLUMN, change.getChangeType());
        assertEquals("orders", change.getTableName());
        assertEquals("status", change.getColumnName());
        assertEquals("varchar(16)", change.getOldType());
        assertEquals(null, change.getNewType());
        assertTrue(change.isBreaking());
        assertEquals("V12__drop_orders_status.sql", change.getSourceFile());
    }
}