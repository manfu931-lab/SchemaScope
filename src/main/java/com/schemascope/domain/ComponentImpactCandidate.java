package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class ComponentImpactCandidate {

    private JavaComponent component;
    private double score;
    private String reason;
    private ImpactRelationLevel relationLevel;
    private List<String> evidencePath = new ArrayList<>();

    public ComponentImpactCandidate() {
    }

    public ComponentImpactCandidate(JavaComponent component, double score, String reason) {
        this(component, score, reason, ImpactRelationLevel.INDIRECT, new ArrayList<>());
    }

    public ComponentImpactCandidate(JavaComponent component,
                                    double score,
                                    String reason,
                                    ImpactRelationLevel relationLevel) {
        this(component, score, reason, relationLevel, new ArrayList<>());
    }

    public ComponentImpactCandidate(JavaComponent component,
                                    double score,
                                    String reason,
                                    ImpactRelationLevel relationLevel,
                                    List<String> evidencePath) {
        this.component = component;
        this.score = score;
        this.reason = reason;
        this.relationLevel = relationLevel;
        this.evidencePath = evidencePath == null ? new ArrayList<>() : new ArrayList<>(evidencePath);
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

    public List<String> getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(List<String> evidencePath) {
        this.evidencePath = evidencePath == null ? new ArrayList<>() : new ArrayList<>(evidencePath);
    }

    @Override
    public String toString() {
        return "ComponentImpactCandidate{" +
                "component=" + component +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                ", relationLevel=" + relationLevel +
                ", evidencePath=" + evidencePath +
                '}';
    }
}