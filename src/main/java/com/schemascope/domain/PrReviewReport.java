package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class PrReviewReport {

    private String projectName;
    private String reviewTitle;
    private String changeSummary;
    private ReviewVerdict verdict;
    private RiskLevel overallRiskLevel;

    private int totalImpactedObjects;
    private int directImpactCount;
    private int indirectImpactCount;

    private List<ImpactResult> topRiskResults = new ArrayList<>();
    private GroupedImpactResults groupedResults;
    private List<ReviewActionItem> actionItems = new ArrayList<>();
    private List<String> reviewChecklist = new ArrayList<>();
    private List<ApiEndpointImpact> impactedEndpoints = new ArrayList<>();
    private List<TestImpactHint> suggestedTests = new ArrayList<>();
    private TestExecutionPlan testExecutionPlan;
    private EvidenceGraphExport evidenceGraph;
    private AiReviewResult aiReview;
    private String markdownComment;

    public PrReviewReport() {
    }

    public PrReviewReport(String projectName,
                          String reviewTitle,
                          String changeSummary,
                          ReviewVerdict verdict,
                          RiskLevel overallRiskLevel,
                          int totalImpactedObjects,
                          int directImpactCount,
                          int indirectImpactCount,
                          List<ImpactResult> topRiskResults,
                          GroupedImpactResults groupedResults,
                          List<ReviewActionItem> actionItems,
                          List<String> reviewChecklist,
                          List<ApiEndpointImpact> impactedEndpoints,
                          List<TestImpactHint> suggestedTests,
                          TestExecutionPlan testExecutionPlan,
                          EvidenceGraphExport evidenceGraph,
                          AiReviewResult aiReview,
                          String markdownComment) {
        this.projectName = projectName;
        this.reviewTitle = reviewTitle;
        this.changeSummary = changeSummary;
        this.verdict = verdict;
        this.overallRiskLevel = overallRiskLevel;
        this.totalImpactedObjects = totalImpactedObjects;
        this.directImpactCount = directImpactCount;
        this.indirectImpactCount = indirectImpactCount;
        this.topRiskResults = topRiskResults == null ? new ArrayList<>() : topRiskResults;
        this.groupedResults = groupedResults;
        this.actionItems = actionItems == null ? new ArrayList<>() : actionItems;
        this.reviewChecklist = reviewChecklist == null ? new ArrayList<>() : reviewChecklist;
        this.impactedEndpoints = impactedEndpoints == null ? new ArrayList<>() : impactedEndpoints;
        this.suggestedTests = suggestedTests == null ? new ArrayList<>() : suggestedTests;
        this.testExecutionPlan = testExecutionPlan;
        this.evidenceGraph = evidenceGraph;
        this.aiReview = aiReview;
        this.markdownComment = markdownComment;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public String getChangeSummary() {
        return changeSummary;
    }

    public void setChangeSummary(String changeSummary) {
        this.changeSummary = changeSummary;
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

    public int getTotalImpactedObjects() {
        return totalImpactedObjects;
    }

    public void setTotalImpactedObjects(int totalImpactedObjects) {
        this.totalImpactedObjects = totalImpactedObjects;
    }

    public int getDirectImpactCount() {
        return directImpactCount;
    }

    public void setDirectImpactCount(int directImpactCount) {
        this.directImpactCount = directImpactCount;
    }

    public int getIndirectImpactCount() {
        return indirectImpactCount;
    }

    public void setIndirectImpactCount(int indirectImpactCount) {
        this.indirectImpactCount = indirectImpactCount;
    }

    public List<ImpactResult> getTopRiskResults() {
        return topRiskResults;
    }

    public void setTopRiskResults(List<ImpactResult> topRiskResults) {
        this.topRiskResults = topRiskResults == null ? new ArrayList<>() : topRiskResults;
    }

    public GroupedImpactResults getGroupedResults() {
        return groupedResults;
    }

    public void setGroupedResults(GroupedImpactResults groupedResults) {
        this.groupedResults = groupedResults;
    }

    public List<ReviewActionItem> getActionItems() {
        return actionItems;
    }

    public void setActionItems(List<ReviewActionItem> actionItems) {
        this.actionItems = actionItems == null ? new ArrayList<>() : actionItems;
    }

    public List<String> getReviewChecklist() {
        return reviewChecklist;
    }

    public void setReviewChecklist(List<String> reviewChecklist) {
        this.reviewChecklist = reviewChecklist == null ? new ArrayList<>() : reviewChecklist;
    }

    public List<ApiEndpointImpact> getImpactedEndpoints() {
        return impactedEndpoints;
    }

    public void setImpactedEndpoints(List<ApiEndpointImpact> impactedEndpoints) {
        this.impactedEndpoints = impactedEndpoints == null ? new ArrayList<>() : impactedEndpoints;
    }

    public List<TestImpactHint> getSuggestedTests() {
        return suggestedTests;
    }

    public void setSuggestedTests(List<TestImpactHint> suggestedTests) {
        this.suggestedTests = suggestedTests == null ? new ArrayList<>() : suggestedTests;
    }

    public TestExecutionPlan getTestExecutionPlan() {
        return testExecutionPlan;
    }

    public void setTestExecutionPlan(TestExecutionPlan testExecutionPlan) {
        this.testExecutionPlan = testExecutionPlan;
    }

    public EvidenceGraphExport getEvidenceGraph() {
        return evidenceGraph;
    }

    public void setEvidenceGraph(EvidenceGraphExport evidenceGraph) {
        this.evidenceGraph = evidenceGraph;
    }

    public AiReviewResult getAiReview() {
        return aiReview;
    }

    public void setAiReview(AiReviewResult aiReview) {
        this.aiReview = aiReview;
    }

    public String getMarkdownComment() {
        return markdownComment;
    }

    public void setMarkdownComment(String markdownComment) {
        this.markdownComment = markdownComment;
    }

    @Override
    public String toString() {
        return "PrReviewReport{" +
                "projectName='" + projectName + '\'' +
                ", reviewTitle='" + reviewTitle + '\'' +
                ", changeSummary='" + changeSummary + '\'' +
                ", verdict=" + verdict +
                ", overallRiskLevel=" + overallRiskLevel +
                ", totalImpactedObjects=" + totalImpactedObjects +
                ", directImpactCount=" + directImpactCount +
                ", indirectImpactCount=" + indirectImpactCount +
                ", topRiskResults=" + topRiskResults +
                ", groupedResults=" + groupedResults +
                ", actionItems=" + actionItems +
                ", reviewChecklist=" + reviewChecklist +
                ", impactedEndpoints=" + impactedEndpoints +
                ", suggestedTests=" + suggestedTests +
                ", testExecutionPlan=" + testExecutionPlan +
                ", evidenceGraph=" + evidenceGraph +
                ", aiReview=" + aiReview +
                ", markdownComment='" + markdownComment + '\'' +
                '}';
    }
}