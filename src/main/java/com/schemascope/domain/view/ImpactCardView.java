package com.schemascope.domain.view;

import java.util.ArrayList;
import java.util.List;

public class ImpactCardView {

    private String affectedObject;
    private String affectedType;
    private String relationLevel;
    private String riskLevel;
    private double confidenceScore;
    private List<String> evidencePath = new ArrayList<>();

    public ImpactCardView() {
    }

    public String getAffectedObject() {
        return affectedObject;
    }

    public void setAffectedObject(String affectedObject) {
        this.affectedObject = affectedObject;
    }

    public String getAffectedType() {
        return affectedType;
    }

    public void setAffectedType(String affectedType) {
        this.affectedType = affectedType;
    }

    public String getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(String relationLevel) {
        this.relationLevel = relationLevel;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public List<String> getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(List<String> evidencePath) {
        this.evidencePath = evidencePath == null ? new ArrayList<>() : evidencePath;
    }

    @Override
    public String toString() {
        return "ImpactCardView{" +
                "affectedObject='" + affectedObject + '\'' +
                ", affectedType='" + affectedType + '\'' +
                ", relationLevel='" + relationLevel + '\'' +
                ", riskLevel='" + riskLevel + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", evidencePath=" + evidencePath +
                '}';
    }
}