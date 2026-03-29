package com.schemascope.service;

import com.schemascope.domain.AiReviewAugmentation;
import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ChangeType;
import com.schemascope.domain.PrReviewReport;
import com.schemascope.domain.SchemaChange;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class HybridAiReviewService implements AiReviewService {

    private final RuleBasedAiReviewService ruleBasedAiReviewService;
    private final OpenAiCompatibleAiReviewClient openAiCompatibleAiReviewClient;
    private final AiReviewAugmentationService aiReviewAugmentationService;
    private final boolean enabled;

    public HybridAiReviewService(RuleBasedAiReviewService ruleBasedAiReviewService,
                                 OpenAiCompatibleAiReviewClient openAiCompatibleAiReviewClient,
                                 @Value("${schemascope.ai.enabled:false}") boolean enabled) {
        this(
                ruleBasedAiReviewService,
                openAiCompatibleAiReviewClient,
                new AiReviewAugmentationService(),
                enabled
        );
    }

    public HybridAiReviewService(RuleBasedAiReviewService ruleBasedAiReviewService,
                                 OpenAiCompatibleAiReviewClient openAiCompatibleAiReviewClient,
                                 AiReviewAugmentationService aiReviewAugmentationService,
                                 boolean enabled) {
        this.ruleBasedAiReviewService = ruleBasedAiReviewService;
        this.openAiCompatibleAiReviewClient = openAiCompatibleAiReviewClient;
        this.aiReviewAugmentationService = aiReviewAugmentationService;
        this.enabled = enabled;
    }

    @Override
    public AiReviewResult review(AnalysisRequest request, PrReviewReport report) {
        if (!enabled) {
            return ruleBasedAiReviewService.review(request, report);
        }

        try {
            AiReviewResult remote = openAiCompatibleAiReviewClient.review(request, report);
            return enrichWithStructuredAugmentation(request, report, remote);
        } catch (Exception e) {
            AiReviewResult fallback = ruleBasedAiReviewService.review(request, report);
            fallback.setExecutiveSummary(
                    fallback.getExecutiveSummary()
                            + " Remote AI was enabled but unavailable, so the system fell back to rule-based review."
            );
            return fallback;
        }
    }

    private AiReviewResult enrichWithStructuredAugmentation(AnalysisRequest request,
                                                            PrReviewReport report,
                                                            AiReviewResult baseResult) {
        AiReviewAugmentation augmentation = aiReviewAugmentationService.augment(
                buildSchemaChangeFromRequest(request),
                report == null ? List.of() : report.getTopRiskResults()
        );

        if (baseResult == null) {
            baseResult = new AiReviewResult();
            baseResult.setProvider("hybrid-ai-review");
            baseResult.setMode("REMOTE_PLUS_RULE_AUGMENT");
        }

        if (baseResult.getExecutiveSummary() == null || baseResult.getExecutiveSummary().isBlank()) {
            baseResult.setExecutiveSummary(augmentation.getSummary());
        }

        baseResult.setKeyRisks(augmentation.getKeyRisks());
        baseResult.setSuggestedActions(augmentation.getSuggestedActions());
        baseResult.setReleaseChecklist(augmentation.getReleaseChecklist());

        baseResult.setRecommendedChecks(mergeDistinct(
                baseResult.getRecommendedChecks(),
                augmentation.getSuggestedActions()
        ));
        baseResult.setReleaseNotes(mergeDistinct(
                baseResult.getReleaseNotes(),
                augmentation.getReleaseChecklist()
        ));

        return baseResult;
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
            return "hybrid-ai-review";
        }

        StringBuilder builder = new StringBuilder("hybrid-ai-review");

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