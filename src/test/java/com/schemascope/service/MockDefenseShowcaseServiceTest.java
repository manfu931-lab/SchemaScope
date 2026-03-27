package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import com.schemascope.service.impl.MockDefenseShowcaseService;
import com.schemascope.service.impl.MockPrReviewService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MockDefenseShowcaseServiceTest {

    @Test
    void shouldBuildDefenseShowcasePack() {
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

        MockPrReviewService reviewService = new MockPrReviewService(
                analysisService,
                new ImpactResultGrouper(),
                new PrReviewReportBuilder(),
                new ImpactSurfaceBuilder(),
                new TestImpactPlanner(),
                new EvidenceGraphExporter()
        );

        MockDefenseShowcaseService showcaseService = new MockDefenseShowcaseService(
                reviewService,
                new DefenseShowcaseBuilder()
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
                "manual-showcase"
        );

        DefenseShowcasePack pack = showcaseService.buildShowcase(request);

        System.out.println("Defense showcase pack = " + pack);

        assertTrue(pack.getMetricCards().size() >= 4);
        assertTrue(pack.getCoreHighlights().stream().anyMatch(line -> line.contains("evidence")));
        assertTrue(pack.getDemoSteps().size() >= 4);
        assertTrue(pack.getDefenseTalkingPoints().size() >= 4);
        assertTrue(pack.getReviewReport() != null);
        assertTrue(pack.getEvidenceGraph() != null);
        assertTrue(pack.getMarkdownBrief().contains("SchemaScope Showcase"));
    }
}