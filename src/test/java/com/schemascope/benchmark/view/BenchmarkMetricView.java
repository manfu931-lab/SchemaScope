package com.schemascope.benchmark.view;

public class BenchmarkMetricView {

    private String label;
    private String value;
    private String tone;

    public BenchmarkMetricView() {
    }

    public BenchmarkMetricView(String label, String value, String tone) {
        this.label = label;
        this.value = value;
        this.tone = tone;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    @Override
    public String toString() {
        return "BenchmarkMetricView{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", tone='" + tone + '\'' +
                '}';
    }
}