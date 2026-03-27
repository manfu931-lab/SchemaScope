package com.schemascope.benchmark;

import com.schemascope.domain.AnalysisRequest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaScopeBenchmarkTest {

    @Test
    void shouldProduceReproducibleBenchmarkMetricsForSqlEvidencePipeline() {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        Path oldSchema = Path.of("src", "test", "resources", "benchmark", "sql-demo-schema", "schema_v1.sql")
                .toAbsolutePath()
                .normalize();

        Path newSchema = Path.of("src", "test", "resources", "benchmark", "sql-demo-schema", "schema_v2.sql")
                .toAbsolutePath()
                .normalize();

        Set<String> expectedAffectedObjects = linkedSet(
                "OwnerJdbcDao",
                "OwnerRepository",
                "OwnerService",
                "OwnerController"
        );

        Set<String> expectedDirectObjects = linkedSet(
                "OwnerJdbcDao",
                "OwnerRepository",
                "OwnerService"
        );

        Set<String> expectedIndirectObjects = linkedSet("OwnerController");

        List<BenchmarkCase> cases = List.of(
                new BenchmarkCase(
                        "manual-drop-column-owners-last-name",
                        "Manual request should recover the full SQL evidence chain",
                        new AnalysisRequest(
                                "sql-demo-project",
                                projectRoot.toString(),
                                null,
                                null,
                                "DROP_COLUMN",
                                "owners",
                                "last_name",
                                "VARCHAR(80)",
                                null,
                                "manual-benchmark"
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                ),
                new BenchmarkCase(
                        "schema-diff-drop-column-owners-last-name",
                        "Schema diff mode should recover the same chain as manual mode",
                        new AnalysisRequest(
                                "sql-demo-project",
                                projectRoot.toString(),
                                oldSchema.toString(),
                                newSchema.toString(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                ),
                new BenchmarkCase(
                        "manual-drop-table-owners",
                        "Table-level change should still recover the main chain",
                        new AnalysisRequest(
                                "sql-demo-project",
                                projectRoot.toString(),
                                null,
                                null,
                                "DROP_TABLE",
                                "owners",
                                null,
                                null,
                                null,
                                "manual-benchmark"
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                )
        );

        SchemaScopeBenchmarkRunner runner = new SchemaScopeBenchmarkRunner();
        BenchmarkSuiteResult suiteResult = runner.runAll(cases);

        System.out.println("SchemaScope benchmark suite result = " + suiteResult);
        suiteResult.getCaseResults().forEach(result ->
                System.out.println("Benchmark case result = " + result)
        );

        assertEquals(3, suiteResult.getCaseResults().size());

        BenchmarkCaseResult manualColumnCase = findCaseResult(suiteResult, "manual-drop-column-owners-last-name");
        BenchmarkCaseResult schemaDiffCase = findCaseResult(suiteResult, "schema-diff-drop-column-owners-last-name");
        BenchmarkCaseResult tableLevelCase = findCaseResult(suiteResult, "manual-drop-table-owners");

        assertTrue(manualColumnCase.getPrecision() >= 0.99,
                "Manual column case precision should be near 1.0, actual=" + manualColumnCase.getPrecision());
        assertTrue(manualColumnCase.getRecall() >= 0.99,
                "Manual column case recall should be near 1.0, actual=" + manualColumnCase.getRecall());
        assertTrue(manualColumnCase.getEvidenceCoverage() >= 0.99,
                "Manual column case evidence coverage should be near 1.0, actual=" + manualColumnCase.getEvidenceCoverage());
        assertTrue(manualColumnCase.getRelationAccuracy() >= 0.99,
                "Manual column case relation accuracy should be near 1.0, actual=" + manualColumnCase.getRelationAccuracy());

        assertTrue(schemaDiffCase.getPrecision() >= 0.99,
                "Schema diff case precision should be near 1.0, actual=" + schemaDiffCase.getPrecision());
        assertTrue(schemaDiffCase.getRecall() >= 0.99,
                "Schema diff case recall should be near 1.0, actual=" + schemaDiffCase.getRecall());
        assertTrue(schemaDiffCase.getEvidenceCoverage() >= 0.99,
                "Schema diff case evidence coverage should be near 1.0, actual=" + schemaDiffCase.getEvidenceCoverage());
        assertTrue(schemaDiffCase.getRelationAccuracy() >= 0.99,
                "Schema diff case relation accuracy should be near 1.0, actual=" + schemaDiffCase.getRelationAccuracy());

        assertTrue(tableLevelCase.getRecall() >= 0.75,
                "Table-level case should recover most of the expected chain, actual=" + tableLevelCase.getRecall());
        assertTrue(tableLevelCase.getEvidenceCoverage() >= 0.75,
                "Table-level case should preserve most evidence signals, actual=" + tableLevelCase.getEvidenceCoverage());
        assertTrue(tableLevelCase.getRelationAccuracy() >= 0.75,
                "Table-level case should preserve most relation labels, actual=" + tableLevelCase.getRelationAccuracy());

        assertTrue(suiteResult.getAveragePrecision() >= 0.90,
                "Expected benchmark precision >= 0.90, actual=" + suiteResult.getAveragePrecision());
        assertTrue(suiteResult.getAverageRecall() >= 0.90,
                "Expected benchmark recall >= 0.90, actual=" + suiteResult.getAverageRecall());
        assertTrue(suiteResult.getEvidenceCoverageRate() >= 0.90,
                "Expected evidence coverage >= 0.90, actual=" + suiteResult.getEvidenceCoverageRate());
        assertTrue(suiteResult.getRelationAccuracyRate() >= 0.90,
                "Expected relation accuracy >= 0.90, actual=" + suiteResult.getRelationAccuracyRate());
        assertTrue(suiteResult.getDirectHitAt3Rate() >= 0.66,
                "Expected most benchmark cases to place direct objects in top3, actual=" + suiteResult.getDirectHitAt3Rate());
    }

    private BenchmarkCaseResult findCaseResult(BenchmarkSuiteResult suiteResult, String caseId) {
        BenchmarkCaseResult result = suiteResult.getCaseResults().stream()
                .filter(item -> caseId.equals(item.getCaseId()))
                .findFirst()
                .orElse(null);

        assertNotNull(result, "Missing benchmark case result for " + caseId);
        return result;
    }

    private Set<String> linkedSet(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }
}