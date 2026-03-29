package com.schemascope.benchmark.view;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkDashboardView {

    private String title;
    private String summary;
    private List<BenchmarkMetricView> metricCards = new ArrayList<>();
    private List<BenchmarkCaseView> caseViews = new ArrayList<>();
    private List<String> highlights = new ArrayList<>();

    public BenchmarkDashboardView() {
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

    public List<BenchmarkMetricView> getMetricCards() {
        return metricCards;
    }

    public void setMetricCards(List<BenchmarkMetricView> metricCards) {
        this.metricCards = metricCards == null ? new ArrayList<>() : metricCards;
    }

    public List<BenchmarkCaseView> getCaseViews() {
        return caseViews;
    }

    public void setCaseViews(List<BenchmarkCaseView> caseViews) {
        this.caseViews = caseViews == null ? new ArrayList<>() : caseViews;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights == null ? new ArrayList<>() : highlights;
    }

    @Override
    public String toString() {
        return "BenchmarkDashboardView{" +
                "title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", metricCards=" + metricCards +
                ", caseViews=" + caseViews +
                ", highlights=" + highlights +
                '}';
    }
}