package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalSchemaToProjectAnalysisTest {

    @Test
    void shouldAnalyzePetclinicSchemasAgainstPetclinicProject() {
        MockAnalysisService service = new MockAnalysisService(
                new SimpleImpactAnalyzer(),
                new SchemaChangeFactory(),
                new SchemaFileReader(),
                new SchemaDiffService(),
                new SpringProjectScanner(),
                new SchemaChangeComponentMapper(),
                new ComponentImpactResultBuilder(),
                new ImpactResultRanker()
        );

        String oldPath = Paths.get("src", "test", "resources", "schema", "petclinic_schema_v1.sql").toString();
        String newPath = Paths.get("src", "test", "resources", "schema", "petclinic_schema_v2.sql").toString();

        AnalysisRequest request = new AnalysisRequest(
                "spring-petclinic",
                "D:/download/SchemaScope/benchmark/spring-petclinic",
                oldPath,
                newPath,
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<ImpactResult> results = service.analyze(request);

        System.out.println(results);

        boolean hasOwner = results.stream()
                .anyMatch(r -> r.getAffectedObject().equals("Owner"));

        boolean hasOwnerController = results.stream()
                .anyMatch(r -> r.getAffectedObject().equals("OwnerController"));

        boolean hasPet = results.stream()
                .anyMatch(r -> r.getAffectedObject().equals("Pet"));

        assertTrue(hasOwner);
        assertTrue(hasOwnerController);
        assertTrue(hasPet);
    }
}