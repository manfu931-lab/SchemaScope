package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import com.schemascope.service.impl.MockPrReviewService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiReviewCompatibilityIntegrationTest {

    @Test
    void shouldExposeStructuredAugmentationInsidePrReviewReportAiReview() {
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
                "manual-review"
        );

        PrReviewReport report = reviewService.review(request);

        System.out.println("Compatibility review report = " + report);

        assertNotNull(report.getAiReview());
        assertFalse(report.getAiReview().getKeyRisks().isEmpty());
        assertFalse(report.getAiReview().getSuggestedActions().isEmpty());
        assertFalse(report.getAiReview().getReleaseChecklist().isEmpty());

        assertTrue(report.getMarkdownComment().contains("Key risks:"));
        assertTrue(report.getMarkdownComment().contains("Suggested actions:"));
        assertTrue(report.getMarkdownComment().contains("Release checklist:"));
    }
}