package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class TestExecutionPlan {

    private List<SelectedTestCase> prioritizedExistingTests = new ArrayList<>();
    private List<SelectedTestCase> missingRecommendedTests = new ArrayList<>();
    private int existingTestCount;
    private int missingTestCount;
    private String summary;

    public TestExecutionPlan() {
    }

    public TestExecutionPlan(List<SelectedTestCase> prioritizedExistingTests,
                             List<SelectedTestCase> missingRecommendedTests,
                             int existingTestCount,
                             int missingTestCount,
                             String summary) {
        this.prioritizedExistingTests = prioritizedExistingTests == null ? new ArrayList<>() : prioritizedExistingTests;
        this.missingRecommendedTests = missingRecommendedTests == null ? new ArrayList<>() : missingRecommendedTests;
        this.existingTestCount = existingTestCount;
        this.missingTestCount = missingTestCount;
        this.summary = summary;
    }

    public List<SelectedTestCase> getPrioritizedExistingTests() {
        return prioritizedExistingTests;
    }

    public void setPrioritizedExistingTests(List<SelectedTestCase> prioritizedExistingTests) {
        this.prioritizedExistingTests = prioritizedExistingTests == null ? new ArrayList<>() : prioritizedExistingTests;
    }

    public List<SelectedTestCase> getMissingRecommendedTests() {
        return missingRecommendedTests;
    }

    public void setMissingRecommendedTests(List<SelectedTestCase> missingRecommendedTests) {
        this.missingRecommendedTests = missingRecommendedTests == null ? new ArrayList<>() : missingRecommendedTests;
    }

    public int getExistingTestCount() {
        return existingTestCount;
    }

    public void setExistingTestCount(int existingTestCount) {
        this.existingTestCount = existingTestCount;
    }

    public int getMissingTestCount() {
        return missingTestCount;
    }

    public void setMissingTestCount(int missingTestCount) {
        this.missingTestCount = missingTestCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "TestExecutionPlan{" +
                "prioritizedExistingTests=" + prioritizedExistingTests +
                ", missingRecommendedTests=" + missingRecommendedTests +
                ", existingTestCount=" + existingTestCount +
                ", missingTestCount=" + missingTestCount +
                ", summary='" + summary + '\'' +
                '}';
    }
}