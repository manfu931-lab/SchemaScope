package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class DefenseShowcaseResult {

    private String projectTitle;
    private String scenarioSummary;
    private List<ImpactResult> impactResults = new ArrayList<>();
    private AiReviewAugmentation aiReviewAugmentation;

    public DefenseShowcaseResult() {
    }

    public DefenseShowcaseResult(String projectTitle,
                                 String scenarioSummary,
                                 List<ImpactResult> impactResults,
                                 AiReviewAugmentation aiReviewAugmentation) {
        this.projectTitle = projectTitle;
        this.scenarioSummary = scenarioSummary;
        this.impactResults = impactResults == null ? new ArrayList<>() : new ArrayList<>(impactResults);
        this.aiReviewAugmentation = aiReviewAugmentation;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getScenarioSummary() {
        return scenarioSummary;
    }

    public void setScenarioSummary(String scenarioSummary) {
        this.scenarioSummary = scenarioSummary;
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
        return "DefenseShowcaseResult{" +
                "projectTitle='" + projectTitle + '\'' +
                ", scenarioSummary='" + scenarioSummary + '\'' +
                ", impactResults=" + impactResults +
                ", aiReviewAugmentation=" + aiReviewAugmentation +
                '}';
    }
}