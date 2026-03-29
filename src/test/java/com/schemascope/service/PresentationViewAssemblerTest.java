package com.schemascope.service;

import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.ReviewVerdict;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.view.ReviewPageView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PresentationViewAssemblerTest {

    @Test
    void shouldConvertPrReviewReportToReviewPageView() {
        PrReviewReport report = new PrReviewReport();
        report.setProjectName("sql-demo-project");
        report.setChangeSummary("DROP_COLUMN owners.last_name");
        report.setTotalImpactedObjects(3);
        report.setDirectImpactCount(2);
        report.setIndirectImpactCount(1);
        report.setOverallRiskLevel(RiskLevel.HIGH);
        report.setVerdict(ReviewVerdict.REVIEW_REQUIRED);
        report.setMarkdownComment("# SchemaScope PR Review");

        ImpactResult top = new ImpactResult(
                "chg-owners-last-name",
                "OwnerRepository",
                "REPOSITORY",
                95.0,
                RiskLevel.HIGH,
                0.95,
                List.of("Matched SQL: OwnerRepository#findByLastName"),
                ImpactRelationLevel.DIRECT
        );
        report.setTopRiskResults(List.of(top));

        AiReviewResult aiReview = new AiReviewResult();
        aiReview.setExecutiveSummary("This change should be reviewed before release.");
        aiReview.setKeyRisks(List.of("Repository query paths may break."));
        aiReview.setSuggestedActions(List.of("Re-run repository regression tests."));
        aiReview.setReleaseChecklist(List.of("Confirm rollout order."));
        report.setAiReview(aiReview);

        PresentationViewAssembler assembler = new PresentationViewAssembler();
        ReviewPageView view = assembler.toReviewPageView(report);

        System.out.println("ReviewPageView = " + view);

        assertEquals("SchemaScope PR Review - sql-demo-project", view.getTitle());
        assertTrue(view.getSummary().contains("reviewed before release"));
        assertEquals("REVIEW_REQUIRED", view.getVerdict());
        assertEquals("HIGH", view.getRiskLevel());
        assertFalse(view.getKeyRisks().isEmpty());
        assertFalse(view.getSuggestedActions().isEmpty());
        assertFalse(view.getReleaseChecklist().isEmpty());
        assertFalse(view.getTopImpactCards().isEmpty());
        assertFalse(view.getMetricCards().isEmpty());
        assertEquals("# SchemaScope PR Review", view.getMarkdownComment());
    }
}