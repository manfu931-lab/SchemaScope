package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalMappedAnalysisServiceTest {

    @Test
    void shouldReturnEvidenceDrivenImpactResultsForLocalFixtureProject() {
        MockAnalysisService service = new MockAnalysisService(
                new SimpleImpactAnalyzer(),
                new SchemaChangeFactory(),
                new SchemaFileReader(),
                new SchemaDiffService(),
                new SpringProjectScanner(),
                new SchemaChangeComponentMapper(),
                new ComponentImpactResultBuilder(),
                new ImpactResultRanker(),
                new SqlAccessExtractor(),
                new SchemaChangeSqlMatcher(),
                new SqlImpactPropagator()
        );

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        AnalysisRequest request = new AnalysisRequest(
                "sql-demo-project",
                projectRoot.toString(),
                null,
                null,
                "DROP_COLUMN",
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                "manual-test"
        );

        List<ImpactResult> results = service.analyze(request);

        System.out.println(results);

        boolean hasOwnerJdbcDao = results.stream()
                .anyMatch(r -> "OwnerJdbcDao".equals(r.getAffectedObject()));

        boolean hasOwnerRepository = results.stream()
                .anyMatch(r -> "OwnerRepository".equals(r.getAffectedObject()));

        boolean hasOwnerService = results.stream()
                .anyMatch(r -> "OwnerService".equals(r.getAffectedObject()));

        boolean hasOwnerController = results.stream()
                .anyMatch(r -> "OwnerController".equals(r.getAffectedObject()));

        boolean hasStructuredEvidence = results.stream()
                .flatMap(r -> r.getEvidencePath().stream())
                .anyMatch(step -> step.contains("Matched SQL:"));

        assertTrue(hasOwnerJdbcDao);
        assertTrue(hasOwnerRepository);
        assertTrue(hasOwnerService);
        assertTrue(hasOwnerController);
        assertTrue(hasStructuredEvidence);
    }
}