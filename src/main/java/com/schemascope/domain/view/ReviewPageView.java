package com.schemascope.domain.view;

import java.util.ArrayList;
import java.util.List;

public class ReviewPageView {

    private String title;
    private String summary;
    private String verdict;
    private String riskLevel;

    private List<String> keyRisks = new ArrayList<>();
    private List<String> suggestedActions = new ArrayList<>();
    private List<String> releaseChecklist = new ArrayList<>();

    private List<ImpactCardView> topImpactCards = new ArrayList<>();
    private List<MetricCardView> metricCards = new ArrayList<>();

    private String markdownComment;

    public ReviewPageView() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public List<ImpactCardView> getTopImpactCards() {
        return topImpactCards;
    }

    public void setTopImpactCards(List<ImpactCardView> topImpactCards) {
        this.topImpactCards = topImpactCards == null ? new ArrayList<>() : topImpactCards;
    }

    public List<MetricCardView> getMetricCards() {
        return metricCards;
    }

    public void setMetricCards(List<MetricCardView> metricCards) {
        this.metricCards = metricCards == null ? new ArrayList<>() : metricCards;
    }

    public String getMarkdownComment() {
        return markdownComment;
    }

    public void setMarkdownComment(String markdownComment) {
        this.markdownComment = markdownComment;
    }

    @Override
    public String toString() {
        return "ReviewPageView{" +
                "title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", verdict='" + verdict + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", keyRisks=" + keyRisks +
                ", suggestedActions=" + suggestedActions +
                ", releaseChecklist=" + releaseChecklist +
                ", topImpactCards=" + topImpactCards +
                ", metricCards=" + metricCards +
                ", markdownComment='" + markdownComment + '\'' +
                '}';
    }
}