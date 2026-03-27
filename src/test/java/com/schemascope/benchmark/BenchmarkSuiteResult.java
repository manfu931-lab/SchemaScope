package com.schemascope.benchmark;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkSuiteResult {

    private final List<BenchmarkCaseResult> caseResults;

    public BenchmarkSuiteResult(List<BenchmarkCaseResult> caseResults) {
        this.caseResults = caseResults == null ? new ArrayList<>() : new ArrayList<>(caseResults);
    }

    public List<BenchmarkCaseResult> getCaseResults() {
        return caseResults;
    }

    public double getAveragePrecision() {
        return averageOf(caseResults.stream().mapToDouble(BenchmarkCaseResult::getPrecision).toArray());
    }

    public double getAverageRecall() {
        return averageOf(caseResults.stream().mapToDouble(BenchmarkCaseResult::getRecall).toArray());
    }

    public double getDirectHitAt3Rate() {
        if (caseResults.isEmpty()) {
            return 0.0;
        }

        long hitCount = caseResults.stream()
                .filter(BenchmarkCaseResult::isDirectHitAt3)
                .count();

        return (double) hitCount / caseResults.size();
    }

    public double getEvidenceCoverageRate() {
        return averageOf(caseResults.stream().mapToDouble(BenchmarkCaseResult::getEvidenceCoverage).toArray());
    }

    public double getRelationAccuracyRate() {
        return averageOf(caseResults.stream().mapToDouble(BenchmarkCaseResult::getRelationAccuracy).toArray());
    }

    private double averageOf(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    @Override
    public String toString() {
        return "BenchmarkSuiteResult{" +
                "caseResults=" + caseResults +
                ", averagePrecision=" + getAveragePrecision() +
                ", averageRecall=" + getAverageRecall() +
                ", directHitAt3Rate=" + getDirectHitAt3Rate() +
                ", evidenceCoverageRate=" + getEvidenceCoverageRate() +
                ", relationAccuracyRate=" + getRelationAccuracyRate() +
                '}';
    }
}