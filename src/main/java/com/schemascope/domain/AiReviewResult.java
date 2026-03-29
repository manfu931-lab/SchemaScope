package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class AiReviewResult {

    private String provider;
    private String mode;
    private String executiveSummary;
    private List<AiReviewFinding> findings = new ArrayList<>();
    private List<String> recommendedChecks = new ArrayList<>();
    private List<String> releaseNotes = new ArrayList<>();

    // 新增：兼容接入结构化增强结果
    private List<String> keyRisks = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private List<String> releaseChecklist = new ArrayList<>();

    public AiReviewResult() {
    }

    public AiReviewResult(String provider,
                          String mode,
                          String executiveSummary,
                          List<AiReviewFinding> findings,
                          List<String> recommendedChecks,
                          List<String> releaseNotes) {
        this.provider = provider;
        this.mode = mode;
        this.executiveSummary = executiveSummary;
        this.findings = findings == null ? new ArrayList<>() : findings;
        this.recommendedChecks = recommendedChecks == null ? new ArrayList<>() : recommendedChecks;
        this.releaseNotes = releaseNotes == null ? new ArrayList<>() : releaseNotes;
    }

    public AiReviewResult(String provider,
                          String mode,
                          String executiveSummary,
                          List<AiReviewFinding> findings,
                          List<String> recommendedChecks,
                          List<String> releaseNotes,
                          List<String> keyRisks,
                          List<String> suggestedActions,
                          List<String> releaseChecklist) {
        this.provider = provider;
        this.mode = mode;
        this.executiveSummary = executiveSummary;
        this.findings = findings == null ? new ArrayList<>() : findings;
        this.recommendedChecks = recommendedChecks == null ? new ArrayList<>() : recommendedChecks;
        this.releaseNotes = releaseNotes == null ? new ArrayList<>() : releaseNotes;
        this.keyRisks = keyRisks == null ? new ArrayList<>() : keyRisks;
        this.suggestedActions = suggestedActions == null ? new ArrayList<>() : suggestedActions;
        this.releaseChecklist = releaseChecklist == null ? new ArrayList<>() : releaseChecklist;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<AiReviewFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<AiReviewFinding> findings) {
        this.findings = findings == null ? new ArrayList<>() : findings;
    }

    public List<String> getRecommendedChecks() {
        return recommendedChecks;
    }

    public void setRecommendedChecks(List<String> recommendedChecks) {
        this.recommendedChecks = recommendedChecks == null ? new ArrayList<>() : recommendedChecks;
    }

    public List<String> getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(List<String> releaseNotes) {
        this.releaseNotes = releaseNotes == null ? new ArrayList<>() : releaseNotes;
    }

    public List<String> getKeyRisks() {
        return keyRisks;
    }

    public void setKeyRisks(List<String> keyRisks) {
        this.keyRisks = keyRisks == null ? new ArrayList<>() : keyRisks;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions == null ? new ArrayList<>() : suggestedActions;
    }

    public List<String> getReleaseChecklist() {
        return releaseChecklist;
    }

    public void setReleaseChecklist(List<String> releaseChecklist) {
        this.releaseChecklist = releaseChecklist == null ? new ArrayList<>() : releaseChecklist;
    }

    @Override
    public String toString() {
        return "AiReviewResult{" +
                "provider='" + provider + '\'' +
                ", mode='" + mode + '\'' +
                ", executiveSummary='" + executiveSummary + '\'' +
                ", findings=" + findings +
                ", recommendedChecks=" + recommendedChecks +
                ", releaseNotes=" + releaseNotes +
                ", keyRisks=" + keyRisks +
                ", suggestedActions=" + suggestedActions +
                ", releaseChecklist=" + releaseChecklist +
                '}';
    }
}