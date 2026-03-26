package com.schemascope.service.impl;

import com.schemascope.domain.*;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.*;
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

    public MockAnalysisService(SimpleImpactAnalyzer simpleImpactAnalyzer,
                               SchemaChangeFactory schemaChangeFactory,
                               SchemaFileReader schemaFileReader,
                               SchemaDiffService schemaDiffService,
                               SpringProjectScanner springProjectScanner,
                               SchemaChangeComponentMapper schemaChangeComponentMapper,
                               ComponentImpactResultBuilder componentImpactResultBuilder,
                               ImpactResultRanker impactResultRanker) {
        this.simpleImpactAnalyzer = simpleImpactAnalyzer;
        this.schemaChangeFactory = schemaChangeFactory;
        this.schemaFileReader = schemaFileReader;
        this.schemaDiffService = schemaDiffService;
        this.springProjectScanner = springProjectScanner;
        this.schemaChangeComponentMapper = schemaChangeComponentMapper;
        this.componentImpactResultBuilder = componentImpactResultBuilder;
        this.impactResultRanker = impactResultRanker;
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
        List<ImpactResult> allResults = new ArrayList<>();

        for (SchemaChange change : changes) {
            List<ComponentImpactCandidate> candidates =
                    schemaChangeComponentMapper.mapCandidates(change, scanResult);

            if (candidates.isEmpty()) {
                allResults.addAll(simpleImpactAnalyzer.analyze(change));
            } else {
                allResults.addAll(componentImpactResultBuilder.build(change, candidates));
            }
        }

        return allResults;
    }

    private boolean hasSchemaPaths(AnalysisRequest request) {
        return request.getOldSchemaPath() != null && !request.getOldSchemaPath().isBlank()
                && request.getNewSchemaPath() != null && !request.getNewSchemaPath().isBlank();
    }

    private boolean hasProjectPath(AnalysisRequest request) {
        return request.getProjectPath() != null && !request.getProjectPath().isBlank();
    }
}