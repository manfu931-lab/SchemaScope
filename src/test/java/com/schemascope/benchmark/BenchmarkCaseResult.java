package com.schemascope.benchmark;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkCaseResult {

    private final String caseId;
    private final List<String> predictedAffectedObjects;
    private final double precision;
    private final double recall;
    private final boolean directHitAt3;
    private final double evidenceCoverage;
    private final double relationAccuracy;

    public BenchmarkCaseResult(String caseId,
                               List<String> predictedAffectedObjects,
                               double precision,
                               double recall,
                               boolean directHitAt3,
                               double evidenceCoverage,
                               double relationAccuracy) {
        this.caseId = caseId;
        this.predictedAffectedObjects = predictedAffectedObjects == null
                ? new ArrayList<>()
                : new ArrayList<>(predictedAffectedObjects);
        this.precision = precision;
        this.recall = recall;
        this.directHitAt3 = directHitAt3;
        this.evidenceCoverage = evidenceCoverage;
        this.relationAccuracy = relationAccuracy;
    }

    public String getCaseId() {
        return caseId;
    }

    public List<String> getPredictedAffectedObjects() {
        return predictedAffectedObjects;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public boolean isDirectHitAt3() {
        return directHitAt3;
    }

    public double getEvidenceCoverage() {
        return evidenceCoverage;
    }

    public double getRelationAccuracy() {
        return relationAccuracy;
    }

    @Override
    public String toString() {
        return "BenchmarkCaseResult{" +
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