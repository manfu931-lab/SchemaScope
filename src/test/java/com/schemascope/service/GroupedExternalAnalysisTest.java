package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.GroupedImpactResults;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupedExternalAnalysisTest {

    @Test
    void shouldGroupMappedResultsForExternalProject() {
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

        ImpactResultGrouper grouper = new ImpactResultGrouper();

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
        GroupedImpactResults grouped = grouper.group(results);

        System.out.println(grouped);

        assertFalse(grouped.getDirectResults().isEmpty());
        assertFalse(grouped.getIndirectResults().isEmpty());

        boolean hasOwnerInDirect = grouped.getDirectResults().stream()
                .anyMatch(r -> r.getAffectedObject().equals("Owner"));

        boolean hasOwnerControllerInIndirect = grouped.getIndirectResults().stream()
                .anyMatch(r -> r.getAffectedObject().equals("OwnerController"));

        assertTrue(hasOwnerInDirect);
        assertTrue(hasOwnerControllerInIndirect);
    }
}