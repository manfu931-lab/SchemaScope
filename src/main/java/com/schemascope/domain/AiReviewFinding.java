package com.schemascope.domain;

public class AiReviewFinding {

    private String category;
    private String title;
    private String detail;
    private RiskLevel riskLevel;
    private double confidence;

    public AiReviewFinding() {
    }

    public AiReviewFinding(String category,
                           String title,
                           String detail,
                           RiskLevel riskLevel,
                           double confidence) {
        this.category = category;
        this.title = title;
        this.detail = detail;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "AiReviewFinding{" +
                "category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", riskLevel=" + riskLevel +
                ", confidence=" + confidence +
                '}';
    }
}