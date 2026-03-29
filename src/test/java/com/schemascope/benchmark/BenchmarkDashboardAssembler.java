package com.schemascope.benchmark;

import com.schemascope.benchmark.view.BenchmarkCaseView;
import com.schemascope.benchmark.view.BenchmarkDashboardView;
import com.schemascope.benchmark.view.BenchmarkMetricView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenchmarkDashboardAssembler {

    public BenchmarkDashboardView toDashboardView(String title, BenchmarkSuiteResult suiteResult) {
        BenchmarkDashboardView view = new BenchmarkDashboardView();

        String effectiveTitle = (title == null || title.isBlank())
                ? "SchemaScope Benchmark Dashboard"
                : title;

        view.setTitle(effectiveTitle);

        if (suiteResult == null) {
            view.setSummary("No benchmark result available.");
            return view;
        }

        view.setSummary(buildSummary(suiteResult));
        view.setMetricCards(buildMetricCards(suiteResult));
        view.setCaseViews(buildCaseViews(suiteResult));
        view.setHighlights(buildHighlights(suiteResult));

        return view;
    }

    private String buildSummary(BenchmarkSuiteResult suiteResult) {
        int caseCount = suiteResult.getCaseResults() == null ? 0 : suiteResult.getCaseResults().size();

        return "Benchmark suite executed "
                + caseCount
                + " case(s). Average precision="
                + formatRate(suiteResult.getAveragePrecision())
                + ", recall="
                + formatRate(suiteResult.getAverageRecall())
                + ", evidence coverage="
                + formatRate(suiteResult.getEvidenceCoverageRate())
                + ", relation accuracy="
                + formatRate(suiteResult.getRelationAccuracyRate())
                + ".";
    }

    private List<BenchmarkMetricView> buildMetricCards(BenchmarkSuiteResult suiteResult) {
        List<BenchmarkMetricView> cards = new ArrayList<>();

        cards.add(new BenchmarkMetricView(
                "Average Precision",
                formatRate(suiteResult.getAveragePrecision()),
                toneForRate(suiteResult.getAveragePrecision())
        ));

        cards.add(new BenchmarkMetricView(
                "Average Recall",
                formatRate(suiteResult.getAverageRecall()),
                toneForRate(suiteResult.getAverageRecall())
        ));

        cards.add(new BenchmarkMetricView(
                "Direct Hit@3",
                formatRate(suiteResult.getDirectHitAt3Rate()),
                toneForRate(suiteResult.getDirectHitAt3Rate())
        ));

        cards.add(new BenchmarkMetricView(
                "Evidence Coverage",
                formatRate(suiteResult.getEvidenceCoverageRate()),
                toneForRate(suiteResult.getEvidenceCoverageRate())
        ));

        cards.add(new BenchmarkMetricView(
                "Relation Accuracy",
                formatRate(suiteResult.getRelationAccuracyRate()),
                toneForRate(suiteResult.getRelationAccuracyRate())
        ));

        return cards;
    }

    private List<BenchmarkCaseView> buildCaseViews(BenchmarkSuiteResult suiteResult) {
        List<BenchmarkCaseView> views = new ArrayList<>();
        if (suiteResult.getCaseResults() == null) {
            return views;
        }

        for (BenchmarkCaseResult result : suiteResult.getCaseResults()) {
            BenchmarkCaseView view = new BenchmarkCaseView();
            view.setCaseId(result.getCaseId());
            view.setPredictedAffectedObjects(result.getPredictedAffectedObjects());
            view.setPrecision(result.getPrecision());
            view.setRecall(result.getRecall());
            view.setDirectHitAt3(result.isDirectHitAt3());
            view.setEvidenceCoverage(result.getEvidenceCoverage());
            view.setRelationAccuracy(result.getRelationAccuracy());
            views.add(view);
        }

        return views;
    }

    private List<String> buildHighlights(BenchmarkSuiteResult suiteResult) {
        List<String> highlights = new ArrayList<>();

        if (suiteResult.getAverageRecall() >= 0.95) {
            highlights.add("Recall is already at a strong research-demo level, which shows the engine can consistently surface true impacted objects.");
        } else if (suiteResult.getAverageRecall() >= 0.80) {
            highlights.add("Recall is acceptable, but still has room for improvement on harder cases.");
        } else {
            highlights.add("Recall is still below the target line and should be improved before defense.");
        }

        if (suiteResult.getEvidenceCoverageRate() >= 0.95) {
            highlights.add("Evidence coverage is strong, which means benchmark hits are backed by explainable trace paths rather than only heuristic ranking.");
        } else {
            highlights.add("Evidence coverage still needs improvement so the system can better justify its predictions.");
        }

        if (suiteResult.getAveragePrecision() >= 0.75) {
            highlights.add("Precision is already in a good zone for defense demonstrations.");
        } else {
            highlights.add("Precision remains the main optimization target for reducing false positives.");
        }

        if (suiteResult.getRelationAccuracyRate() >= 0.95) {
            highlights.add("Relation accuracy is strong, indicating that propagation links remain trustworthy across the current suite.");
        }

        return highlights;
    }

    private String formatRate(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String toneForRate(double value) {
        if (value >= 0.90) {
            return "excellent";
        }
        if (value >= 0.75) {
            return "good";
        }
        if (value >= 0.60) {
            return "warning";
        }
        return "danger";
    }
}