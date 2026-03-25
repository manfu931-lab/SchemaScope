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
                "schema_v2.sql"
        );

        assertEquals("petclinic", request.getProjectName());
        assertEquals("/benchmark/petclinic", request.getProjectPath());
        assertEquals("schema_v1.sql", request.getOldSchemaPath());
        assertEquals("schema_v2.sql", request.getNewSchemaPath());
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