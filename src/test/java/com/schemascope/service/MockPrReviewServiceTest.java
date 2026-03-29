package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.ReviewVerdict;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import com.schemascope.service.impl.MockPrReviewService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockPrReviewServiceTest {

    @Test
    void shouldBuildPullRequestReviewReportWithAiReview() {
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

        System.out.println("PR review report = " + report);

        assertEquals("sql-demo-project", report.getProjectName());
        assertTrue(report.getChangeSummary().contains("owners.last_name"));
        assertTrue(report.getTotalImpactedObjects() >= 3);
        assertTrue(report.getDirectImpactCount() >= 2);
        assertTrue(report.getIndirectImpactCount() >= 1);

        assertNotNull(report.getEvidenceGraph());
        assertNotNull(report.getAiReview());
        assertNotNull(report.getMarkdownComment());

        assertTrue(report.getAiReview().getExecutiveSummary() != null
                && !report.getAiReview().getExecutiveSummary().isBlank());

        assertTrue(report.getMarkdownComment().contains("SchemaScope PR Review"));
        assertTrue(report.getMarkdownComment().contains("Evidence"));
        assertTrue(report.getTopRiskResults().stream()
                .anyMatch(result -> "OwnerRepository".equals(result.getAffectedObject())));

        assertTrue(report.getVerdict() == ReviewVerdict.REVIEW_REQUIRED
                || report.getVerdict() == ReviewVerdict.BLOCK
                || report.getVerdict() == ReviewVerdict.APPROVE);
    }
}