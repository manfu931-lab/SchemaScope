package com.schemascope.domain.view;

import java.util.ArrayList;
import java.util.List;

public class ShowcaseDashboardView {

    private String title;
    private String executiveSummary;
    private String verdict;
    private String riskLevel;

    private List<MetricCardView> metricCards = new ArrayList<>();
    private List<String> coreHighlights = new ArrayList<>();
    private List<String> demoSteps = new ArrayList<>();
    private List<String> defenseTalkingPoints = new ArrayList<>();

    private ReviewPageView reviewPage;
    private String markdownBrief;

    public ShowcaseDashboardView() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<MetricCardView> getMetricCards() {
        return metricCards;
    }

    public void setMetricCards(List<MetricCardView> metricCards) {
        this.metricCards = metricCards == null ? new ArrayList<>() : metricCards;
    }

    public List<String> getCoreHighlights() {
        return coreHighlights;
    }

    public void setCoreHighlights(List<String> coreHighlights) {
        this.coreHighlights = coreHighlights == null ? new ArrayList<>() : coreHighlights;
    }

    public List<String> getDemoSteps() {
        return demoSteps;
    }

    public void setDemoSteps(List<String> demoSteps) {
        this.demoSteps = demoSteps == null ? new ArrayList<>() : demoSteps;
    }

    public List<String> getDefenseTalkingPoints() {
        return defenseTalkingPoints;
    }

    public void setDefenseTalkingPoints(List<String> defenseTalkingPoints) {
        this.defenseTalkingPoints = defenseTalkingPoints == null ? new ArrayList<>() : defenseTalkingPoints;
    }

    public ReviewPageView getReviewPage() {
        return reviewPage;
    }

    public void setReviewPage(ReviewPageView reviewPage) {
        this.reviewPage = reviewPage;
    }

    public String getMarkdownBrief() {
        return markdownBrief;
    }

    public void setMarkdownBrief(String markdownBrief) {
        this.markdownBrief = markdownBrief;
    }

    @Override
    public String toString() {
        return "ShowcaseDashboardView{" +
                "title='" + title + '\'' +
                ", executiveSummary='" + executiveSummary + '\'' +
                ", verdict='" + verdict + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", metricCards=" + metricCards +
                ", coreHighlights=" + coreHighlights +
                ", demoSteps=" + demoSteps +
                ", defenseTalkingPoints=" + defenseTalkingPoints +
                ", reviewPage=" + reviewPage +
                ", markdownBrief='" + markdownBrief + '\'' +
                '}';
    }
}