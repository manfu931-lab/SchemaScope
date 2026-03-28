package com.schemascope.service;

import com.schemascope.domain.AiReviewResult;
import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.PrReviewReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HybridAiReviewService implements AiReviewService {

    private final RuleBasedAiReviewService ruleBasedAiReviewService;
    private final OpenAiCompatibleAiReviewClient openAiCompatibleAiReviewClient;
    private final boolean enabled;

    public HybridAiReviewService(RuleBasedAiReviewService ruleBasedAiReviewService,
                                 OpenAiCompatibleAiReviewClient openAiCompatibleAiReviewClient,
                                 @Value("${schemascope.ai.enabled:false}") boolean enabled) {
        this.ruleBasedAiReviewService = ruleBasedAiReviewService;
        this.openAiCompatibleAiReviewClient = openAiCompatibleAiReviewClient;
        this.enabled = enabled;
    }

    @Override
    public AiReviewResult review(AnalysisRequest request, PrReviewReport report) {
        if (!enabled) {
            return ruleBasedAiReviewService.review(request, report);
        }

        try {
            return openAiCompatibleAiReviewClient.review(request, report);
        } catch (Exception e) {
            AiReviewResult fallback = ruleBasedAiReviewService.review(request, report);
            fallback.setExecutiveSummary(
                    fallback.getExecutiveSummary() + " Remote AI was enabled but unavailable, so the system fell back to rule-based review."
            );
            return fallback;
        }
    }
}