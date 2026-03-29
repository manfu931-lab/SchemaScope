package com.schemascope.service;

import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.DefenseMetricCard;
import com.schemascope.domain.DefenseShowcasePack;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.view.ImpactCardView;
import com.schemascope.domain.view.MetricCardView;
import com.schemascope.domain.view.ReviewPageView;
import com.schemascope.domain.view.ShowcaseDashboardView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PresentationViewAssembler {

    public ReviewPageView toReviewPageView(PrReviewReport report) {
        ReviewPageView view = new ReviewPageView();

        if (report == null) {
            view.setTitle("SchemaScope Review");
            view.setSummary("No review data available.");
            view.setVerdict("UNKNOWN");
            view.setRiskLevel("UNKNOWN");
            return view;
        }

        view.setTitle("SchemaScope PR Review - " + safe(report.getProjectName()));
        view.setSummary(extractSummary(report.getAiReview(), report.getChangeSummary()));
        view.setVerdict(report.getVerdict() == null ? "UNKNOWN" : report.getVerdict().name());
        view.setRiskLevel(report.getOverallRiskLevel() == null ? "UNKNOWN" : report.getOverallRiskLevel().name());
        view.setMarkdownComment(report.getMarkdownComment());

        if (report.getAiReview() != null) {
            view.setKeyRisks(report.getAiReview().getKeyRisks());
            view.setSuggestedActions(report.getAiReview().getSuggestedActions());
            view.setReleaseChecklist(report.getAiReview().getReleaseChecklist());
        }

        view.setTopImpactCards(buildImpactCards(report.getTopRiskResults()));
        view.setMetricCards(buildMetricCards(report));

        return view;
    }

    public ShowcaseDashboardView toShowcaseDashboardView(DefenseShowcasePack pack) {
        ShowcaseDashboardView view = new ShowcaseDashboardView();

        if (pack == null) {
            view.setTitle("SchemaScope Showcase");
            view.setExecutiveSummary("No showcase data available.");
            view.setVerdict("UNKNOWN");
            view.setRiskLevel("UNKNOWN");
            return view;
        }

        view.setTitle(pack.getDemoTitle() == null || pack.getDemoTitle().isBlank()
                ? "SchemaScope Showcase"
                : pack.getDemoTitle());
        view.setExecutiveSummary(pack.getExecutiveSummary());
        view.setVerdict(pack.getVerdict() == null ? "UNKNOWN" : pack.getVerdict().name());
        view.setRiskLevel(pack.getOverallRiskLevel() == null ? "UNKNOWN" : pack.getOverallRiskLevel().name());
        view.setMetricCards(convertDefenseMetricCards(pack.getMetricCards()));
        view.setCoreHighlights(pack.getCoreHighlights());
        view.setDemoSteps(pack.getDemoSteps());
        view.setDefenseTalkingPoints(pack.getDefenseTalkingPoints());
        view.setReviewPage(toReviewPageView(pack.getReviewReport()));
        view.setMarkdownBrief(pack.getMarkdownBrief());

        return view;
    }

    private String extractSummary(AiReviewResult aiReview, String fallback) {
        if (aiReview != null && aiReview.getExecutiveSummary() != null && !aiReview.getExecutiveSummary().isBlank()) {
            return aiReview.getExecutiveSummary();
        }
        return fallback == null ? "No summary available." : fallback;
    }

    private List<ImpactCardView> buildImpactCards(List<ImpactResult> impactResults) {
        List<ImpactCardView> cards = new ArrayList<>();
        if (impactResults == null) {
            return cards;
        }

        for (ImpactResult result : impactResults) {
            ImpactCardView card = new ImpactCardView();
            card.setAffectedObject(result.getAffectedObject());
            card.setAffectedType(result.getAffectedType());
            card.setRelationLevel(result.getRelationLevel() == null ? "UNKNOWN" : result.getRelationLevel().name());
            card.setRiskLevel(result.getRiskLevel() == null ? "UNKNOWN" : result.getRiskLevel().name());
            card.setConfidenceScore(result.getConfidence());
            card.setEvidencePath(result.getEvidencePath());
            cards.add(card);
        }

        return cards;
    }

    private List<MetricCardView> buildMetricCards(PrReviewReport report) {
        List<MetricCardView> cards = new ArrayList<>();

        cards.add(new MetricCardView(
                "Total Impacted",
                String.valueOf(report.getTotalImpactedObjects()),
                toneForCount(report.getTotalImpactedObjects())
        ));

        cards.add(new MetricCardView(
                "Direct Impact",
                String.valueOf(report.getDirectImpactCount()),
                toneForCount(report.getDirectImpactCount())
        ));

        cards.add(new MetricCardView(
                "Indirect Impact",
                String.valueOf(report.getIndirectImpactCount()),
                toneForCount(report.getIndirectImpactCount())
        ));

        cards.add(new MetricCardView(
                "Risk Level",
                report.getOverallRiskLevel() == null ? "UNKNOWN" : report.getOverallRiskLevel().name(),
                toneForRisk(report.getOverallRiskLevel() == null ? "UNKNOWN" : report.getOverallRiskLevel().name())
        ));

        cards.add(new MetricCardView(
                "Verdict",
                report.getVerdict() == null ? "UNKNOWN" : report.getVerdict().name(),
                toneForVerdict(report.getVerdict() == null ? "UNKNOWN" : report.getVerdict().name())
        ));

        return cards;
    }

    private List<MetricCardView> convertDefenseMetricCards(List<DefenseMetricCard> sourceCards) {
        List<MetricCardView> cards = new ArrayList<>();
        if (sourceCards == null) {
            return cards;
        }

        for (DefenseMetricCard source : sourceCards) {
            cards.add(new MetricCardView(
                    source.getTitle(),
                    source.getValue(),
                    source.getDetail()
            ));
        }

        return cards;
    }

    private String toneForCount(int count) {
        if (count >= 5) {
            return "high";
        }
        if (count >= 2) {
            return "medium";
        }
        return "low";
    }

    private String toneForRisk(String risk) {
        if ("HIGH".equalsIgnoreCase(risk)) {
            return "danger";
        }
        if ("MEDIUM".equalsIgnoreCase(risk)) {
            return "warning";
        }
        return "safe";
    }

    private String toneForVerdict(String verdict) {
        if ("BLOCK".equalsIgnoreCase(verdict)) {
            return "danger";
        }
        if ("REVIEW_REQUIRED".equalsIgnoreCase(verdict)) {
            return "warning";
        }
        return "safe";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown-project" : value;
    }
}