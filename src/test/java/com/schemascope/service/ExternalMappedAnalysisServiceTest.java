package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalMappedAnalysisServiceTest {

    @Test
    void shouldReturnMappedImpactResultsForExternalProject() {
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

        AnalysisRequest request = new AnalysisRequest(
                "spring-petclinic",
                "D:/download/SchemaScope/benchmark/spring-petclinic",
                null,
                null,
                "DROP_COLUMN",
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                "manual-test"
        );

        List<ImpactResult> results = service.analyze(request);

        System.out.println(results);

        boolean hasOwner = results.stream()
                .anyMatch(r -> r.getAffectedObject().equals("Owner"));

        boolean hasOwnerController = results.stream()
                .anyMatch(r -> r.getAffectedObject().equals("OwnerController"));

        assertTrue(hasOwner);
        assertTrue(results.get(0).getAffectedObject().equals("Owner"));
        assertTrue(hasOwnerController);
    }
}