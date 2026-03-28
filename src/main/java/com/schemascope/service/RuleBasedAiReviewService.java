package com.schemascope.service;

import com.schemascope.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuleBasedAiReviewService {

    public AiReviewResult review(AnalysisRequest request, PrReviewReport report) {
        List<AiReviewFinding> findings = new ArrayList<>();
        List<String> recommendedChecks = new ArrayList<>();
        List<String> releaseNotes = new ArrayList<>();

        if (report == null) {
            return new AiReviewResult(
                    "schemascope-rule-engine",
                    "RULE_BASED_FALLBACK",
                    "No review report available, so AI augmentation could not be generated.",
                    findings,
                    recommendedChecks,
                    releaseNotes
            );
        }

        if (isBreakingChange(request)) {
            findings.add(new AiReviewFinding(
                    "SCHEMA_BREAKING_CHANGE",
                    "Breaking schema change",
                    "This request removes a table or column, so compatibility and rollout sequencing must be checked carefully.",
                    RiskLevel.HIGH,
                    0.96
            ));
            recommendedChecks.add("Verify backward compatibility window for old readers and writers.");
            recommendedChecks.add("Confirm rollback script and emergency restore procedure before merge.");
            releaseNotes.add("Coordinate application rollout order with schema deployment to avoid incompatible readers.");
        }

        if (report.getDirectImpactCount() > 0) {
            findings.add(new AiReviewFinding(
                    "DIRECT_IMPACT_CHAIN",
                    "Direct impact chain detected",
                    "The change has direct impact on " + report.getDirectImpactCount()
                            + " components, which means data access and service behavior should be reviewed first.",
                    RiskLevel.HIGH,
                    0.92
            ));
            recommendedChecks.add("Review repositories / DAOs and the first-hop services before broader regression.");
        }

        if (report.getImpactedEndpoints() != null && !report.getImpactedEndpoints().isEmpty()) {
            findings.add(new AiReviewFinding(
                    "API_REGRESSION_RISK",
                    "API regression surface detected",
                    "The propagated chain reaches API endpoints, so request/response level regression testing is recommended.",
                    RiskLevel.MEDIUM,
                    0.88
            ));
            recommendedChecks.add("Run endpoint-level regression for impacted APIs, including negative cases and null/empty data cases.");
        }

        if (report.getTestExecutionPlan() != null
                && report.getTestExecutionPlan().getMissingTestCount() > 0) {
            findings.add(new AiReviewFinding(
                    "TEST_GAP",
                    "Missing focused regression tests",
                    "There are " + report.getTestExecutionPlan().getMissingTestCount()
                            + " recommended tests that are not currently present in the project.",
                    RiskLevel.MEDIUM,
                    0.84
            ));
            recommendedChecks.add("Add or update focused regression tests before large-scale rollout.");
        }

        if (report.getEvidenceGraph() != null
                && report.getEvidenceGraph().getNodes() != null
                && report.getEvidenceGraph().getNodes().size() >= 5) {
            findings.add(new AiReviewFinding(
                    "CROSS_LAYER_PROPAGATION",
                    "Cross-layer propagation confirmed",
                    "The evidence graph shows that this schema change propagates across multiple layers, not just one repository or controller.",
                    RiskLevel.MEDIUM,
                    0.81
            ));
        }

        if (recommendedChecks.isEmpty()) {
            recommendedChecks.add("Perform baseline repository and API smoke checks.");
        }

        if (releaseNotes.isEmpty()) {
            releaseNotes.add("No additional release note beyond normal validation was generated.");
        }

        String summary = buildSummary(report, findings);

        return new AiReviewResult(
                "schemascope-rule-engine",
                "RULE_BASED_FALLBACK",
                summary,
                findings,
                recommendedChecks,
                releaseNotes
        );
    }

    private boolean isBreakingChange(AnalysisRequest request) {
        if (request == null || request.getChangeType() == null) {
            return false;
        }

        return "DROP_COLUMN".equalsIgnoreCase(request.getChangeType())
                || "DROP_TABLE".equalsIgnoreCase(request.getChangeType());
    }

    private String buildSummary(PrReviewReport report, List<AiReviewFinding> findings) {
        StringBuilder sb = new StringBuilder();
        sb.append("AI fallback review concludes that ")
                .append(report.getChangeSummary())
                .append(" should be treated as ")
                .append(report.getVerdict())
                .append(" with overall risk ")
                .append(report.getOverallRiskLevel())
                .append(". ");

        if (!findings.isEmpty()) {
            sb.append("Primary concerns include ");
            for (int i = 0; i < findings.size() && i < 3; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(findings.get(i).getTitle().toLowerCase());
            }
            sb.append(".");
        }

        return sb.toString();
    }
}