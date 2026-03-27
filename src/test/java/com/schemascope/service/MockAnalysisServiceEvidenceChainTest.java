package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactRelationLevel;
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

class MockAnalysisServiceEvidenceChainTest {

    @Test
    void shouldReturnEvidenceDrivenImpactResultsForFixtureProject() {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

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

        System.out.println("MockAnalysisService evidence-driven results = " + results);

        boolean hasOwnerRepository = results.stream().anyMatch(result ->
                "OwnerRepository".equals(result.getAffectedObject())
                        && result.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && result.getEvidencePath().stream().anyMatch(step -> step.contains("Matched SQL:"))
                        && result.getEvidencePath().stream().anyMatch(step -> step.contains("SQL snippet:"))
        );

        boolean hasOwnerService = results.stream().anyMatch(result ->
                "OwnerService".equals(result.getAffectedObject())
                        && result.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && result.getEvidencePath().stream().anyMatch(step ->
                        step.contains("Propagation: OwnerService references OwnerRepository"))
        );

        boolean hasOwnerController = results.stream().anyMatch(result ->
                "OwnerController".equals(result.getAffectedObject())
                        && result.getRelationLevel() == ImpactRelationLevel.INDIRECT
                        && result.getEvidencePath().stream().anyMatch(step ->
                        step.contains("Propagation: OwnerController references OwnerService"))
        );

        boolean usesSchemaChangeHeader = results.stream().allMatch(result ->
                result.getEvidencePath().stream().anyMatch(step ->
                        step.contains("Schema change: DROP_COLUMN owners.last_name"))
        );

        assertTrue(hasOwnerRepository, "Expected OwnerRepository evidence chain result");
        assertTrue(hasOwnerService, "Expected OwnerService propagated evidence chain result");
        assertTrue(hasOwnerController, "Expected OwnerController propagated evidence chain result");
        assertTrue(usesSchemaChangeHeader, "Expected schema change header in every evidence path");
    }
}