package com.schemascope.domain;

public class ReviewActionItem {

    private String title;
    private String detail;
    private RiskLevel priority;

    public ReviewActionItem() {
    }

    public ReviewActionItem(String title, String detail, RiskLevel priority) {
        this.title = title;
        this.detail = detail;
        this.priority = priority;
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

    public RiskLevel getPriority() {
        return priority;
    }

    public void setPriority(RiskLevel priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "ReviewActionItem{" +
                "title='" + title + '\'' +
                ", detail='" + detail + '\'' +
                ", priority=" + priority +
                '}';
    }
}