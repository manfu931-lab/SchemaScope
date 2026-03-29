package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class PrReviewResult {

    private String title;
    private String changeSummary;
    private List<ImpactResult> impactResults = new ArrayList<>();
    private AiReviewAugmentation aiReviewAugmentation;

    public PrReviewResult() {
    }

    public PrReviewResult(String title,
                          String changeSummary,
                          List<ImpactResult> impactResults,
                          AiReviewAugmentation aiReviewAugmentation) {
        this.title = title;
        this.changeSummary = changeSummary;
        this.impactResults = impactResults == null ? new ArrayList<>() : new ArrayList<>(impactResults);
        this.aiReviewAugmentation = aiReviewAugmentation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
    }

    public List<ImpactResult> getImpactResults() {
        return impactResults;
    }

    public void setImpactResults(List<ImpactResult> impactResults) {
        this.impactResults = impactResults == null ? new ArrayList<>() : new ArrayList<>(impactResults);
    }

    public AiReviewAugmentation getAiReviewAugmentation() {
        return aiReviewAugmentation;
    }

    public void setAiReviewAugmentation(AiReviewAugmentation aiReviewAugmentation) {
        this.aiReviewAugmentation = aiReviewAugmentation;
    }

    @Override
    public String toString() {
        return "PrReviewResult{" +
                "title='" + title + '\'' +
                ", changeSummary='" + changeSummary + '\'' +
                ", impactResults=" + impactResults +
                ", aiReviewAugmentation=" + aiReviewAugmentation +
                '}';
    }
}