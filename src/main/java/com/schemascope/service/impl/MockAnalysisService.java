package com.schemascope.service.impl;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlImpactCandidate;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.AnalysisService;
import com.schemascope.service.ComponentImpactResultBuilder;
import com.schemascope.service.ImpactResultRanker;
import com.schemascope.service.SchemaChangeComponentMapper;
import com.schemascope.service.SchemaChangeFactory;
import com.schemascope.service.SchemaChangeSqlMatcher;
import com.schemascope.service.SimpleImpactAnalyzer;
import com.schemascope.service.SqlImpactPropagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockAnalysisService implements AnalysisService {

    private final ImpactResultRanker impactResultRanker;
    private final SimpleImpactAnalyzer simpleImpactAnalyzer;
    private final SchemaChangeFactory schemaChangeFactory;
    private final SchemaFileReader schemaFileReader;
    private final SchemaDiffService schemaDiffService;
    private final SpringProjectScanner springProjectScanner;
    private final SchemaChangeComponentMapper schemaChangeComponentMapper;
    private final ComponentImpactResultBuilder componentImpactResultBuilder;
    private final SqlAccessExtractor sqlAccessExtractor;
    private final SchemaChangeSqlMatcher schemaChangeSqlMatcher;
    private final SqlImpactPropagator sqlImpactPropagator;

    public MockAnalysisService(SimpleImpactAnalyzer simpleImpactAnalyzer,
                               SchemaChangeFactory schemaChangeFactory,
                               SchemaFileReader schemaFileReader,
                               SchemaDiffService schemaDiffService,
                               SpringProjectScanner springProjectScanner,
                               SchemaChangeComponentMapper schemaChangeComponentMapper,
                               ComponentImpactResultBuilder componentImpactResultBuilder,
                               ImpactResultRanker impactResultRanker) {
        this(simpleImpactAnalyzer,
                schemaChangeFactory,
                schemaFileReader,
                schemaDiffService,
                springProjectScanner,
                schemaChangeComponentMapper,
                componentImpactResultBuilder,
                impactResultRanker,
                new SqlAccessExtractor(),
                new SchemaChangeSqlMatcher(),
                new SqlImpactPropagator());
    }

    @Autowired
    public MockAnalysisService(SimpleImpactAnalyzer simpleImpactAnalyzer,
                               SchemaChangeFactory schemaChangeFactory,
                               SchemaFileReader schemaFileReader,
                               SchemaDiffService schemaDiffService,
                               SpringProjectScanner springProjectScanner,
                               SchemaChangeComponentMapper schemaChangeComponentMapper,
                               ComponentImpactResultBuilder componentImpactResultBuilder,
                               ImpactResultRanker impactResultRanker,
                               SqlAccessExtractor sqlAccessExtractor,
                               SchemaChangeSqlMatcher schemaChangeSqlMatcher,
                               SqlImpactPropagator sqlImpactPropagator) {
        this.simpleImpactAnalyzer = simpleImpactAnalyzer;
        this.schemaChangeFactory = schemaChangeFactory;
        this.schemaFileReader = schemaFileReader;
        this.schemaDiffService = schemaDiffService;
        this.springProjectScanner = springProjectScanner;
        this.schemaChangeComponentMapper = schemaChangeComponentMapper;
        this.componentImpactResultBuilder = componentImpactResultBuilder;
        this.impactResultRanker = impactResultRanker;
        this.sqlAccessExtractor = sqlAccessExtractor;
        this.schemaChangeSqlMatcher = schemaChangeSqlMatcher;
        this.sqlImpactPropagator = sqlImpactPropagator;
    }

    @Override
    public List<ImpactResult> analyze(AnalysisRequest request) {
        try {
            List<ImpactResult> rawResults;

            if (hasSchemaPaths(request)) {
                ParsedSchema oldSchema = schemaFileReader.read(request.getOldSchemaPath());
                ParsedSchema newSchema = schemaFileReader.read(request.getNewSchemaPath());

                List<SchemaChange> changes = schemaDiffService.diff(oldSchema, newSchema);
                rawResults = analyzeChangesAgainstProject(request.getProjectPath(), changes);
            } else {
                SchemaChange schemaChange = schemaChangeFactory.fromRequest(request);

                if (hasProjectPath(request)) {
                    rawResults = analyzeChangesAgainstProject(request.getProjectPath(), List.of(schemaChange));
                } else {
                    rawResults = simpleImpactAnalyzer.analyze(schemaChange);
                }
            }

            return impactResultRanker.rank(rawResults, 5);

        } catch (Exception e) {
            throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
        }
    }

    private List<ImpactResult> analyzeChangesAgainstProject(String projectPath, List<SchemaChange> changes) throws Exception {
        JavaProjectScanResult scanResult = springProjectScanner.scan(projectPath);
        List<SqlAccessPoint> accessPoints = sqlAccessExtractor.extractFromProject(projectPath);

        List<ImpactResult> allResults = new ArrayList<>();

        for (SchemaChange change : changes) {
            List<ImpactResult> evidenceDrivenResults = analyzeWithSqlEvidence(change, scanResult, accessPoints);

            if (!evidenceDrivenResults.isEmpty()) {
                allResults.addAll(evidenceDrivenResults);
                continue;
            }

            List<ComponentImpactCandidate> heuristicCandidates =
                    schemaChangeComponentMapper.mapCandidates(change, scanResult);

            if (heuristicCandidates.isEmpty()) {
                allResults.addAll(simpleImpactAnalyzer.analyze(change));
            } else {
                allResults.addAll(componentImpactResultBuilder.build(change, heuristicCandidates));
            }
        }

        return allResults;
    }

    private List<ImpactResult> analyzeWithSqlEvidence(SchemaChange change,
                                                      JavaProjectScanResult scanResult,
                                                      List<SqlAccessPoint> accessPoints) throws Exception {
        List<SqlImpactCandidate> sqlCandidates = schemaChangeSqlMatcher.match(change, accessPoints);
        if (sqlCandidates.isEmpty()) {
            return List.of();
        }

        List<ComponentImpactCandidate> propagatedCandidates =
                sqlImpactPropagator.propagate(sqlCandidates, scanResult);

        if (propagatedCandidates.isEmpty()) {
            return List.of();
        }

        return componentImpactResultBuilder.build(change, propagatedCandidates);
    }

    private boolean hasSchemaPaths(AnalysisRequest request) {
        return request.getOldSchemaPath() != null && !request.getOldSchemaPath().isBlank()
                && request.getNewSchemaPath() != null && !request.getNewSchemaPath().isBlank();
    }

    private boolean hasProjectPath(AnalysisRequest request) {
        return request.getProjectPath() != null && !request.getProjectPath().isBlank();
    }
}