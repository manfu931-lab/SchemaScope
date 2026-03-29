package com.schemascope.benchmark.view;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkCaseView {

    private String caseId;
    private List<String> predictedAffectedObjects = new ArrayList<>();
    private double precision;
    private double recall;
    private boolean directHitAt3;
    private double evidenceCoverage;
    private double relationAccuracy;

    public BenchmarkCaseView() {
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public List<String> getPredictedAffectedObjects() {
        return predictedAffectedObjects;
    }

    public void setPredictedAffectedObjects(List<String> predictedAffectedObjects) {
        this.predictedAffectedObjects = predictedAffectedObjects == null ? new ArrayList<>() : predictedAffectedObjects;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public boolean isDirectHitAt3() {
        return directHitAt3;
    }

    public void setDirectHitAt3(boolean directHitAt3) {
        this.directHitAt3 = directHitAt3;
    }

    public double getEvidenceCoverage() {
        return evidenceCoverage;
    }

    public void setEvidenceCoverage(double evidenceCoverage) {
        this.evidenceCoverage = evidenceCoverage;
    }

    public double getRelationAccuracy() {
        return relationAccuracy;
    }

    public void setRelationAccuracy(double relationAccuracy) {
        this.relationAccuracy = relationAccuracy;
    }

    @Override
    public String toString() {
        return "BenchmarkCaseView{" +
                "caseId='" + caseId + '\'' +
                ", predictedAffectedObjects=" + predictedAffectedObjects +
                ", precision=" + precision +
                ", recall=" + recall +
                ", directHitAt3=" + directHitAt3 +
                ", evidenceCoverage=" + evidenceCoverage +
                ", relationAccuracy=" + relationAccuracy +
                '}';
    }
}