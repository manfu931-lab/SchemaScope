package com.schemascope.service;

import com.schemascope.domain.ApiEndpointImpact;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.ImpactSurfaceSummary;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SelectedTestCase;
import com.schemascope.domain.TestExecutionPlan;
import com.schemascope.domain.TestImpactHint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestImpactPlanner {

    public TestExecutionPlan buildPlan(List<ImpactResult> results, ImpactSurfaceSummary surfaceSummary) {
        List<ImpactResult> safeResults = results == null ? new ArrayList<>() : results;
        ImpactSurfaceSummary safeSurface = surfaceSummary == null
                ? new ImpactSurfaceSummary(new ArrayList<>(), new ArrayList<>())
                : surfaceSummary;

        Map<String, SelectedTestCase> merged = new LinkedHashMap<>();

        for (TestImpactHint hint : safeSurface.getSuggestedTests()) {
            SelectedTestCase testCase = toSelectedTestCase(hint, safeResults, safeSurface.getImpactedEndpoints());
            merged.put(testCase.getTestClassName(), testCase);
        }

        List<SelectedTestCase> existing = new ArrayList<>();
        List<SelectedTestCase> missing = new ArrayList<>();

        for (SelectedTestCase testCase : merged.values()) {
            if (testCase.isExistingTest()) {
                existing.add(testCase);
            } else {
                missing.add(testCase);
            }
        }

        existing.sort(Comparator.comparing(SelectedTestCase::getScore).reversed()
                .thenComparing(SelectedTestCase::getTestClassName));

        missing.sort(Comparator.comparing(SelectedTestCase::getScore).reversed()
                .thenComparing(SelectedTestCase::getTestClassName));

        return new TestExecutionPlan(
                existing,
                missing,
                existing.size(),
                missing.size(),
                buildSummary(existing, missing)
        );
    }

    private SelectedTestCase toSelectedTestCase(TestImpactHint hint,
                                                List<ImpactResult> results,
                                                List<ApiEndpointImpact> endpoints) {
        double score = baseScore(hint.getPriority());

        for (ImpactResult result : results) {
            if (referencesAffectedObject(hint, result.getAffectedObject())) {
                score += result.getRelationLevel() == ImpactRelationLevel.DIRECT ? 0.20 : 0.08;
                score += toRiskBonus(result.getRiskLevel());
            }
        }

        for (ApiEndpointImpact endpoint : endpoints) {
            if (referencesAffectedObject(hint, endpoint.getOwnerController())) {
                score += 0.10;
            }
        }

        score = Math.min(score, 1.0);

        return new SelectedTestCase(
                hint.getTestClassName(),
                hint.getFilePath(),
                score,
                toPriority(score),
                hint.getFilePath() != null && !hint.getFilePath().isBlank(),
                buildReason(hint, score)
        );
    }

    private boolean referencesAffectedObject(TestImpactHint hint, String affectedObject) {
        if (affectedObject == null || affectedObject.isBlank()) {
            return false;
        }

        String testName = hint.getTestClassName() == null ? "" : hint.getTestClassName();
        String reason = hint.getReason() == null ? "" : hint.getReason();

        return testName.contains(affectedObject) || reason.contains(affectedObject);
    }

    private double baseScore(RiskLevel priority) {
        if (priority == RiskLevel.HIGH) {
            return 0.72;
        }
        if (priority == RiskLevel.MEDIUM) {
            return 0.55;
        }
        return 0.35;
    }

    private double toRiskBonus(RiskLevel riskLevel) {
        if (riskLevel == RiskLevel.HIGH) {
            return 0.10;
        }
        if (riskLevel == RiskLevel.MEDIUM) {
            return 0.05;
        }
        return 0.02;
    }

    private RiskLevel toPriority(double score) {
        if (score >= 0.80) {
            return RiskLevel.HIGH;
        }
        if (score >= 0.60) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private String buildReason(TestImpactHint hint, double score) {
        StringBuilder sb = new StringBuilder();
        sb.append(hint.getReason());
        sb.append("; ranked score=").append(String.format("%.2f", score));
        if (hint.getFilePath() == null || hint.getFilePath().isBlank()) {
            sb.append("; missing focused existing test");
        } else {
            sb.append("; existing focused test available");
        }
        return sb.toString();
    }

    private String buildSummary(List<SelectedTestCase> existing, List<SelectedTestCase> missing) {
        if (existing.isEmpty() && missing.isEmpty()) {
            return "No targeted test execution plan generated.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Prioritized existing tests: ").append(existing.size());
        sb.append(", missing recommended tests: ").append(missing.size());

        if (!existing.isEmpty()) {
            sb.append(", top existing: ").append(existing.get(0).getTestClassName());
        }

        if (!missing.isEmpty()) {
            sb.append(", top missing: ").append(missing.get(0).getTestClassName());
        }

        return sb.toString();
    }
}