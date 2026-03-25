package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RealSchemaAnalysisServiceTest {

    @Test
    void shouldAnalyzeFromRealSchemaFiles() {
        SimpleImpactAnalyzer analyzer = new SimpleImpactAnalyzer();
        SchemaChangeFactory factory = new SchemaChangeFactory();
        SchemaFileReader reader = new SchemaFileReader();
        SchemaDiffService diffService = new SchemaDiffService();

        MockAnalysisService service = new MockAnalysisService(
                analyzer,
                factory,
                reader,
                diffService
        );

        String oldPath = Paths.get("src", "test", "resources", "schema", "schema_v1.sql").toString();
        String newPath = Paths.get("src", "test", "resources", "schema", "schema_v2.sql").toString();

        AnalysisRequest request = new AnalysisRequest(
                "demo-project",
                "/benchmark/demo-project",
                oldPath,
                newPath,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<ImpactResult> results = service.analyze(request);

        assertFalse(results.isEmpty());

        boolean containsDropColumnImpact = results.stream()
                .anyMatch(r -> r.getChangeId().contains("drop-column-orders-status"));

        boolean containsAlterTypeImpact = results.stream()
                .anyMatch(r -> r.getChangeId().contains("alter-column-type-users-email"));

        assertTrue(containsDropColumnImpact);
        assertTrue(containsAlterTypeImpact);
    }
}