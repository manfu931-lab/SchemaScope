package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.ImpactSurfaceSummary;
import com.schemascope.domain.TestExecutionPlan;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestImpactPlannerTest {

    @Test
    void shouldPrioritizeExistingTestsBeforeMissingRecommendedTests() throws Exception {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        MockAnalysisService analysisService = new MockAnalysisService(
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
                "manual-test-plan"
        );

        List<ImpactResult> results = analysisService.analyze(request);

        ImpactSurfaceBuilder impactSurfaceBuilder = new ImpactSurfaceBuilder();
        ImpactSurfaceSummary surfaceSummary = impactSurfaceBuilder.build(projectRoot.toString(), results);

        TestImpactPlanner planner = new TestImpactPlanner();
        TestExecutionPlan plan = planner.buildPlan(results, surfaceSummary);

        System.out.println("Test execution plan = " + plan);

        assertFalse(plan.getPrioritizedExistingTests().isEmpty(), "Expected prioritized existing tests");
        assertTrue(plan.getPrioritizedExistingTests().stream().anyMatch(testCase ->
                "OwnerControllerTest".equals(testCase.getTestClassName())));
        assertTrue(plan.getPrioritizedExistingTests().stream().anyMatch(testCase ->
                "OwnerServiceTest".equals(testCase.getTestClassName())));
        assertTrue(plan.getPrioritizedExistingTests().stream().anyMatch(testCase ->
                "OwnerJdbcDaoTest".equals(testCase.getTestClassName())));
        assertTrue(plan.getPrioritizedExistingTests().get(0).isExistingTest());
        assertTrue(plan.getSummary().contains("Prioritized existing tests"));
    }
}