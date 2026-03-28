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

    @Override
    public String toString() {
        return "AiReviewResult{" +
                "provider='" + provider + '\'' +
                ", mode='" + mode + '\'' +
                ", executiveSummary='" + executiveSummary + '\'' +
                ", findings=" + findings +
                ", recommendedChecks=" + recommendedChecks +
                ", releaseNotes=" + releaseNotes +
                '}';
    }
}