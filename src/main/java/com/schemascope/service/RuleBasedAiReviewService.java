package com.schemascope.service;

import com.schemascope.domain.AiReviewAugmentation;
import com.schemascope.domain.AiReviewFinding;
import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ChangeType;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RuleBasedAiReviewService {

    private final AiReviewAugmentationService aiReviewAugmentationService;

    public RuleBasedAiReviewService() {
        this(new AiReviewAugmentationService());
    }

    @Autowired
    public RuleBasedAiReviewService(AiReviewAugmentationService aiReviewAugmentationService) {
        this.aiReviewAugmentationService = aiReviewAugmentationService;
    }

    public AiReviewResult review(AnalysisRequest request, PrReviewReport report) {
        List<AiReviewFinding> findings = new ArrayList<>();
        List<String> recommendedChecks = new ArrayList<>();
        List<String> releaseNotes = new ArrayList<>();

        if (report == null) {
            AiReviewResult fallback = new AiReviewResult(
                    "schemascope-rule-engine",
                    "RULE_BASED_FALLBACK",
                    "No review report available, so AI augmentation could not be generated.",
                    findings,
                    recommendedChecks,
                    releaseNotes
            );
            fallback.setKeyRisks(List.of("No structured review report was available, so downstream AI augmentation could not be grounded in evidence."));
            fallback.setSuggestedActions(List.of("Generate the PR review report first, then re-run AI augmentation."));
            fallback.setReleaseChecklist(List.of("Do not rely on AI review output until the underlying evidence report is available."));
            return fallback;
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

        AiReviewAugmentation augmentation = aiReviewAugmentationService.augment(
                buildSchemaChangeFromRequest(request),
                report.getTopRiskResults()
        );

        String summary = buildSummary(report, findings, augmentation);

        AiReviewResult result = new AiReviewResult(
                "schemascope-rule-engine",
                "RULE_BASED_FALLBACK",
                summary,
                findings,
                mergeDistinct(recommendedChecks, augmentation.getSuggestedActions()),
                mergeDistinct(releaseNotes, augmentation.getReleaseChecklist()),
                augmentation.getKeyRisks(),
                augmentation.getSuggestedActions(),
                augmentation.getReleaseChecklist()
        );

        return result;
    }

    private boolean isBreakingChange(AnalysisRequest request) {
        if (request == null || request.getChangeType() == null) {
            return false;
        }

        return "DROP_COLUMN".equalsIgnoreCase(request.getChangeType())
                || "DROP_TABLE".equalsIgnoreCase(request.getChangeType());
    }

    private String buildSummary(PrReviewReport report,
                                List<AiReviewFinding> findings,
                                AiReviewAugmentation augmentation) {
        if (augmentation != null
                && augmentation.getSummary() != null
                && !augmentation.getSummary().isBlank()) {
            return augmentation.getSummary();
        }

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

    private SchemaChange buildSchemaChangeFromRequest(AnalysisRequest request) {
        ChangeType changeType = null;
        if (request != null && request.getChangeType() != null && !request.getChangeType().isBlank()) {
            try {
                changeType = ChangeType.valueOf(request.getChangeType());
            } catch (IllegalArgumentException ignored) {
                changeType = null;
            }
        }

        return new SchemaChange(
                buildChangeId(request),
                changeType,
                request == null ? null : request.getTableName(),
                request == null ? null : request.getColumnName(),
                request == null ? null : request.getOldType(),
                request == null ? null : request.getNewType(),
                true,
                request == null ? null : request.getSourceFile()
        );
    }

    private String buildChangeId(AnalysisRequest request) {
        if (request == null) {
            return "ai-review";
        }

        StringBuilder builder = new StringBuilder("ai-review");

        if (request.getChangeType() != null && !request.getChangeType().isBlank()) {
            builder.append("-").append(request.getChangeType().toLowerCase());
        }

        if (request.getTableName() != null && !request.getTableName().isBlank()) {
            builder.append("-").append(request.getTableName().toLowerCase());
        }

        if (request.getColumnName() != null && !request.getColumnName().isBlank()) {
            builder.append("-").append(request.getColumnName().toLowerCase());
        }

        return builder.toString();
    }

    private List<String> mergeDistinct(List<String> base, List<String> extra) {
        Set<String> merged = new LinkedHashSet<>();
        if (base != null) {
            merged.addAll(base);
        }
        if (extra != null) {
            merged.addAll(extra);
        }
        return new ArrayList<>(merged);
    }
}