package com.schemascope.service.impl;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.ParsedSchema;
import com.schemascope.domain.SchemaChange;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.AnalysisService;
import com.schemascope.service.SchemaChangeFactory;
import com.schemascope.service.SimpleImpactAnalyzer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MockAnalysisService implements AnalysisService {

    private final SimpleImpactAnalyzer simpleImpactAnalyzer;
    private final SchemaChangeFactory schemaChangeFactory;
    private final SchemaFileReader schemaFileReader;
    private final SchemaDiffService schemaDiffService;

    public MockAnalysisService(SimpleImpactAnalyzer simpleImpactAnalyzer,
                               SchemaChangeFactory schemaChangeFactory,
                               SchemaFileReader schemaFileReader,
                               SchemaDiffService schemaDiffService) {
        this.simpleImpactAnalyzer = simpleImpactAnalyzer;
        this.schemaChangeFactory = schemaChangeFactory;
        this.schemaFileReader = schemaFileReader;
        this.schemaDiffService = schemaDiffService;
    }

    @Override
    public List<ImpactResult> analyze(AnalysisRequest request) {
        try {
            if (hasSchemaPaths(request)) {
                ParsedSchema oldSchema = schemaFileReader.read(request.getOldSchemaPath());
                ParsedSchema newSchema = schemaFileReader.read(request.getNewSchemaPath());

                List<SchemaChange> changes = schemaDiffService.diff(oldSchema, newSchema);
                List<ImpactResult> allResults = new ArrayList<>();

                for (SchemaChange change : changes) {
                    allResults.addAll(simpleImpactAnalyzer.analyze(change));
                }

                return allResults;
            }

            SchemaChange schemaChange = schemaChangeFactory.fromRequest(request);
            return simpleImpactAnalyzer.analyze(schemaChange);

        } catch (Exception e) {
            throw new RuntimeException("Analysis failed: " + e.getMessage(), e);
        }
    }

    private boolean hasSchemaPaths(AnalysisRequest request) {
        return request.getOldSchemaPath() != null && !request.getOldSchemaPath().isBlank()
                && request.getNewSchemaPath() != null && !request.getNewSchemaPath().isBlank();
    }
}