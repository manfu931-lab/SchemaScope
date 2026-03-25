package com.schemascope.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalysisDomainTest {

    @Test
    void shouldCreateAnalysisRequestCorrectly() {
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

        assertEquals("petclinic", request.getProjectName());
        assertEquals("/benchmark/petclinic", request.getProjectPath());
        assertEquals("schema_v1.sql", request.getOldSchemaPath());
        assertEquals("schema_v2.sql", request.getNewSchemaPath());
        assertEquals("DROP_COLUMN", request.getChangeType());
        assertEquals("orders", request.getTableName());
        assertEquals("status", request.getColumnName());
        assertEquals("varchar(16)", request.getOldType());
        assertEquals(null, request.getNewType());
        assertEquals("V12__drop_orders_status.sql", request.getSourceFile());
    }

    @Test
    void shouldCreateImpactResultCorrectly() {
        ImpactResult result = new ImpactResult(
                "chg-001",
                "/api/orders/list",
                "API",
                87.5,
                RiskLevel.HIGH,
                0.91,
                Arrays.asList(
                        "orders.status",
                        "OrderRepository.queryByStatus",
                        "OrderService.listOrders",
                        "/api/orders/list"
                )
        );

        assertEquals("chg-001", result.getChangeId());
        assertEquals("/api/orders/list", result.getAffectedObject());
        assertEquals("API", result.getAffectedType());
        assertEquals(87.5, result.getRiskScore());
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertEquals(0.91, result.getConfidence());
        assertEquals(4, result.getEvidencePath().size());
    }
}