package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class DefenseShowcasePack {

    private String projectName;
    private String demoTitle;
    private String executiveSummary;
    private ReviewVerdict verdict;
    private RiskLevel overallRiskLevel;

    private List<DefenseMetricCard> metricCards = new ArrayList<>();
    private List<String> coreHighlights = new ArrayList<>();
    private List<String> demoSteps = new ArrayList<>();
    private List<String> defenseTalkingPoints = new ArrayList<>();

    private PrReviewReport reviewReport;
    private EvidenceGraphExport evidenceGraph;
    private String markdownBrief;

    public DefenseShowcasePack() {
    }

    public DefenseShowcasePack(String projectName,
                               String demoTitle,
                               String executiveSummary,
                               ReviewVerdict verdict,
                               RiskLevel overallRiskLevel,
                               List<DefenseMetricCard> metricCards,
                               List<String> coreHighlights,
                               List<String> demoSteps,
                               List<String> defenseTalkingPoints,
                               PrReviewReport reviewReport,
                               EvidenceGraphExport evidenceGraph,
                               String markdownBrief) {
        this.projectName = projectName;
        this.demoTitle = demoTitle;
        this.executiveSummary = executiveSummary;
        this.verdict = verdict;
        this.overallRiskLevel = overallRiskLevel;
        this.metricCards = metricCards == null ? new ArrayList<>() : metricCards;
        this.coreHighlights = coreHighlights == null ? new ArrayList<>() : coreHighlights;
        this.demoSteps = demoSteps == null ? new ArrayList<>() : demoSteps;
        this.defenseTalkingPoints = defenseTalkingPoints == null ? new ArrayList<>() : defenseTalkingPoints;
        this.reviewReport = reviewReport;
        this.evidenceGraph = evidenceGraph;
        this.markdownBrief = markdownBrief;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDemoTitle() {
        return demoTitle;
    }

    public void setDemoTitle(String demoTitle) {
        this.demoTitle = demoTitle;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public ReviewVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(ReviewVerdict verdict) {
        this.verdict = verdict;
    }

    public RiskLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(RiskLevel overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
    }

    public List<DefenseMetricCard> getMetricCards() {
        return metricCards;
    }

    public void setMetricCards(List<DefenseMetricCard> metricCards) {
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

    public PrReviewReport getReviewReport() {
        return reviewReport;
    }

    public void setReviewReport(PrReviewReport reviewReport) {
        this.reviewReport = reviewReport;
    }

    public EvidenceGraphExport getEvidenceGraph() {
        return evidenceGraph;
    }

    public void setEvidenceGraph(EvidenceGraphExport evidenceGraph) {
        this.evidenceGraph = evidenceGraph;
    }

    public String getMarkdownBrief() {
        return markdownBrief;
    }

    public void setMarkdownBrief(String markdownBrief) {
        this.markdownBrief = markdownBrief;
    }

    @Override
    public String toString() {
        return "DefenseShowcasePack{" +
                "projectName='" + projectName + '\'' +
                ", demoTitle='" + demoTitle + '\'' +
                ", executiveSummary='" + executiveSummary + '\'' +
                ", verdict=" + verdict +
                ", overallRiskLevel=" + overallRiskLevel +
                ", metricCards=" + metricCards +
                ", coreHighlights=" + coreHighlights +
                ", demoSteps=" + demoSteps +
                ", defenseTalkingPoints=" + defenseTalkingPoints +
                ", reviewReport=" + reviewReport +
                ", evidenceGraph=" + evidenceGraph +
                ", markdownBrief='" + markdownBrief + '\'' +
                '}';
    }
}