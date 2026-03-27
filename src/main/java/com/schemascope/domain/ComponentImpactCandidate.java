package com.schemascope.domain;

public class ComponentImpactCandidate {

    private JavaComponent component;
    private double score;
    private String reason;
    private ImpactRelationLevel relationLevel;

    public ComponentImpactCandidate() {
    }

    public ComponentImpactCandidate(JavaComponent component, double score, String reason) {
        this.component = component;
        this.score = score;
        this.reason = reason;
        this.relationLevel = ImpactRelationLevel.INDIRECT;
    }

    public ComponentImpactCandidate(JavaComponent component,
                                    double score,
                                    String reason,
                                    ImpactRelationLevel relationLevel) {
        this.component = component;
        this.score = score;
        this.reason = reason;
        this.relationLevel = relationLevel;
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

    public ImpactRelationLevel getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(ImpactRelationLevel relationLevel) {
        this.relationLevel = relationLevel;
    }

    @Override
    public String toString() {
        return "ComponentImpactCandidate{" +
                "component=" + component +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                ", relationLevel=" + relationLevel +
                '}';
    }
}