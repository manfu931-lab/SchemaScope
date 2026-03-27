package com.schemascope.domain;

public class DefenseMetricCard {

    private String title;
    private String value;
    private String detail;

    public DefenseMetricCard() {
    }

    public DefenseMetricCard(String title, String value, String detail) {
        this.title = title;
        this.value = value;
        this.detail = detail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "DefenseMetricCard{" +
                "title='" + title + '\'' +
                ", value='" + value + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}