package com.schemascope.domain;

public class ComponentImpactCandidate {

    private JavaComponent component;
    private double score;
    private String reason;

    public ComponentImpactCandidate() {
    }

    public ComponentImpactCandidate(JavaComponent component, double score, String reason) {
        this.component = component;
        this.score = score;
        this.reason = reason;
    }

    public JavaComponent getComponent() {
        return component;
    }

    public void setComponent(JavaComponent component) {
        this.component = component;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ComponentImpactCandidate{" +
                "component=" + component +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                '}';
    }
}