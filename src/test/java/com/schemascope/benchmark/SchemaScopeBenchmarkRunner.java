package com.schemascope.benchmark;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.ComponentImpactResultBuilder;
import com.schemascope.service.ImpactResultRanker;
import com.schemascope.service.SchemaChangeComponentMapper;
import com.schemascope.service.SchemaChangeFactory;
import com.schemascope.service.SchemaChangeSqlMatcher;
import com.schemascope.service.SimpleImpactAnalyzer;
import com.schemascope.service.SqlImpactPropagator;
import com.schemascope.service.impl.MockAnalysisService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SchemaScopeBenchmarkRunner {

    private final MockAnalysisService service;

    public SchemaScopeBenchmarkRunner() {
        this.service = new MockAnalysisService(
                new SimpleImpactAnalyzer(),
                new SchemaChangeFactory(),
                new SchemaFileReader(),
                new SchemaDiffService(),
                new SpringProjectScanner(),
                new SchemaChangeComponentMapper(),
                new ComponentImpactResultBuilder(),
                new ImpactResultRanker(),
                new SqlAccessExtractor(),
                new SchemaChangeSqlMatcher(),
                new SqlImpactPropagator()
        );
    }

    public BenchmarkSuiteResult runAll(List<BenchmarkCase> benchmarkCases) {
        List<BenchmarkCaseResult> caseResults = new ArrayList<>();

        for (BenchmarkCase benchmarkCase : benchmarkCases) {
            caseResults.add(runCase(benchmarkCase));
        }

        return new BenchmarkSuiteResult(caseResults);
    }

    public BenchmarkCaseResult runCase(BenchmarkCase benchmarkCase) {
        AnalysisRequest request = benchmarkCase.getRequest();
        List<ImpactResult> results = service.analyze(request);

        List<String> predictedObjectsInRankOrder = results.stream()
                .map(ImpactResult::getAffectedObject)
                .toList();

        Set<String> predictedSet = new LinkedHashSet<>(predictedObjectsInRankOrder);
        Set<String> expectedSet = new LinkedHashSet<>(benchmarkCase.getExpectedAffectedObjects());

        Set<String> truePositives = new LinkedHashSet<>(predictedSet);
        truePositives.retainAll(expectedSet);

        double precision = predictedSet.isEmpty()
                ? 0.0
                : (double) truePositives.size() / predictedSet.size();

        double recall = expectedSet.isEmpty()
                ? 1.0
                : (double) truePositives.size() / expectedSet.size();

        Set<String> top3 = new LinkedHashSet<>(predictedObjectsInRankOrder.stream().limit(3).toList());
        boolean directHitAt3 = top3.containsAll(benchmarkCase.getExpectedDirectObjects());

        double evidenceCoverage = computeEvidenceCoverage(benchmarkCase, results);
        double relationAccuracy = computeRelationAccuracy(benchmarkCase, results);

        return new BenchmarkCaseResult(
                benchmarkCase.getCaseId(),
                predictedObjectsInRankOrder,
                precision,
                recall,
                directHitAt3,
                evidenceCoverage,
                relationAccuracy
        );
    }

    private double computeEvidenceCoverage(BenchmarkCase benchmarkCase, List<ImpactResult> results) {
        if (!benchmarkCase.isRequireEvidencePath()) {
            return 1.0;
        }

        if (benchmarkCase.getExpectedAffectedObjects().isEmpty()) {
            return 1.0;
        }

        int covered = 0;

        for (String expectedObject : benchmarkCase.getExpectedAffectedObjects()) {
            ImpactResult matched = findByAffectedObject(results, expectedObject);
            if (matched == null || matched.getEvidencePath() == null || matched.getEvidencePath().isEmpty()) {
                continue;
            }

            boolean hasEvidenceSignal = matched.getEvidencePath().stream().anyMatch(step ->
                    step.contains("Matched SQL:")
                            || step.contains("Propagation:")
                            || step.contains("SQL snippet:")
            );

            if (hasEvidenceSignal) {
                covered++;
            }
        }

        return (double) covered / benchmarkCase.getExpectedAffectedObjects().size();
    }

    private double computeRelationAccuracy(BenchmarkCase benchmarkCase, List<ImpactResult> results) {
        int expectedCount = benchmarkCase.getExpectedDirectObjects().size()
                + benchmarkCase.getExpectedIndirectObjects().size();

        if (expectedCount == 0) {
            return 1.0;
        }

        int correct = 0;

        for (String expectedDirect : benchmarkCase.getExpectedDirectObjects()) {
            ImpactResult matched = findByAffectedObject(results, expectedDirect);
            if (matched != null && matched.getRelationLevel() == ImpactRelationLevel.DIRECT) {
                correct++;
            }
        }

        for (String expectedIndirect : benchmarkCase.getExpectedIndirectObjects()) {
            ImpactResult matched = findByAffectedObject(results, expectedIndirect);
            if (matched != null && matched.getRelationLevel() == ImpactRelationLevel.INDIRECT) {
                correct++;
            }
        }

        return (double) correct / expectedCount;
    }

    private ImpactResult findByAffectedObject(List<ImpactResult> results, String affectedObject) {
        return results.stream()
                .filter(result -> affectedObject.equals(result.getAffectedObject()))
                .findFirst()
                .orElse(null);
    }
}