package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
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
        MockAnalysisService service = new MockAnalysisService(
                new SimpleImpactAnalyzer(),
                new SchemaChangeFactory(),
                new SchemaFileReader(),
                new SchemaDiffService(),
                new SpringProjectScanner(),
                new SchemaChangeComponentMapper(),
                new ComponentImpactResultBuilder()
        );

        String oldPath = Paths.get("src", "test", "resources", "schema", "schema_v1.sql").toString();
        String newPath = Paths.get("src", "test", "resources", "schema", "schema_v2.sql").toString();
        String currentProjectRoot = Paths.get(".").toAbsolutePath().normalize().toString();

        AnalysisRequest request = new AnalysisRequest(
                "schemascope-self",
                currentProjectRoot,
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

        System.out.println(results);

        assertFalse(results.isEmpty());

        boolean containsDropColumnImpact = results.stream()
                .anyMatch(r -> r.getChangeId().contains("drop-column-orders-status"));

        boolean containsAlterTypeImpact = results.stream()
                .anyMatch(r -> r.getChangeId().contains("alter-column-type-users-email"));

        assertTrue(containsDropColumnImpact);
        assertTrue(containsAlterTypeImpact);
    }
}