package com.schemascope.benchmark;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaScopeBenchmarkTest {

    @Test
    void shouldProduceStableMetricsAcrossMultipleFixtureProjects() {
        BenchmarkProjectCatalog catalog = new BenchmarkProjectCatalog();
        List<BenchmarkProjectSpec> projectSpecs = catalog.loadAll();

        SchemaScopeBenchmarkRunner runner = new SchemaScopeBenchmarkRunner();
        BenchmarkSuiteResult suiteResult = runner.runProjectSpecs(projectSpecs);

        System.out.println("SchemaScope benchmark suite result = " + suiteResult);
        suiteResult.getCaseResults().forEach(result ->
                System.out.println("Benchmark case result = " + result)
        );

        assertEquals(2, projectSpecs.size(), "Expected two local benchmark projects");
        assertEquals(6, suiteResult.getCaseResults().size(), "Expected 6 benchmark cases in total");

        assertTrue(suiteResult.getAveragePrecision() >= 0.88,
                "Expected average precision >= 0.88, actual=" + suiteResult.getAveragePrecision());
        assertTrue(suiteResult.getAverageRecall() >= 0.88,
                "Expected average recall >= 0.88, actual=" + suiteResult.getAverageRecall());
        assertTrue(suiteResult.getEvidenceCoverageRate() >= 0.88,
                "Expected evidence coverage >= 0.88, actual=" + suiteResult.getEvidenceCoverageRate());
        assertTrue(suiteResult.getRelationAccuracyRate() >= 0.88,
                "Expected relation accuracy >= 0.88, actual=" + suiteResult.getRelationAccuracyRate());
        assertTrue(suiteResult.getDirectHitAt3Rate() >= 0.66,
                "Expected most benchmark cases to place direct objects in top3, actual=" + suiteResult.getDirectHitAt3Rate());

        boolean hasSqlDemoCases = suiteResult.getCaseResults().stream()
                .anyMatch(result -> result.getCaseId().startsWith("sql-demo-project-"));

        boolean hasOrderDemoCases = suiteResult.getCaseResults().stream()
                .anyMatch(result -> result.getCaseId().startsWith("order-demo-project-"));

        assertTrue(hasSqlDemoCases, "Expected sql-demo-project benchmark cases");
        assertTrue(hasOrderDemoCases, "Expected order-demo-project benchmark cases");
    }
}