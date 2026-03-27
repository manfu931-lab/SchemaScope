package com.schemascope.domain;

import java.util.List;

public class ImpactResult {

    private String changeId;
    private String affectedObject;
    private String affectedType;
    private double riskScore;
    private RiskLevel riskLevel;
    private double confidence;
    private List<String> evidencePath;
    private ImpactRelationLevel relationLevel;

    public ImpactResult() {
    }

    public ImpactResult(String changeId,
                        String affectedObject,
                        String affectedType,
                        double riskScore,
                        RiskLevel riskLevel,
                        double confidence,
                        List<String> evidencePath) {
        this.changeId = changeId;
        this.affectedObject = affectedObject;
        this.affectedType = affectedType;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.evidencePath = evidencePath;
        this.relationLevel = ImpactRelationLevel.INDIRECT;
    }

    public ImpactResult(String changeId,
                        String affectedObject,
                        String affectedType,
                        double riskScore,
                        RiskLevel riskLevel,
                        double confidence,
                        List<String> evidencePath,
                        ImpactRelationLevel relationLevel) {
        this.changeId = changeId;
        this.affectedObject = affectedObject;
        this.affectedType = affectedType;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.evidencePath = evidencePath;
        this.relationLevel = relationLevel;
    }

    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(String changeId) {
        this.changeId = changeId;
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

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
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

    public List<String> getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(List<String> evidencePath) {
        this.evidencePath = evidencePath;
    }

    public ImpactRelationLevel getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(ImpactRelationLevel relationLevel) {
        this.relationLevel = relationLevel;
    }

    @Override
    public String toString() {
        return "ImpactResult{" +
                "changeId='" + changeId + '\'' +
                ", affectedObject='" + affectedObject + '\'' +
                ", affectedType='" + affectedType + '\'' +
                ", riskScore=" + riskScore +
                ", riskLevel=" + riskLevel +
                ", confidence=" + confidence +
                ", evidencePath=" + evidencePath +
                ", relationLevel=" + relationLevel +
                '}';
    }
}