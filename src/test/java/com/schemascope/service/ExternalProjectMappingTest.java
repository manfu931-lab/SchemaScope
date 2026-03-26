package com.schemascope.service;

import com.schemascope.domain.*;
import com.schemascope.parser.SpringProjectScanner;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProjectMappingTest {

    @Test
    void shouldMapOwnerTableChangeToPetclinicComponents() throws Exception {
        SpringProjectScanner scanner = new SpringProjectScanner();
        SchemaChangeComponentMapper mapper = new SchemaChangeComponentMapper();

        String projectRoot = "D:/download/SchemaScope/benchmark/spring-petclinic";
        JavaProjectScanResult scanResult = scanner.scan(projectRoot);

        SchemaChange change = new SchemaChange(
                "chg-drop-column-owners-last-name",
                ChangeType.DROP_COLUMN,
                "owners",
                "last_name",
                "last_name VARCHAR(80)",
                null,
                true,
                "schema-diff"
        );

        List<ComponentImpactCandidate> candidates = mapper.mapCandidates(change, scanResult);

        System.out.println(candidates);

        boolean hasOwner = candidates.stream()
                .anyMatch(candidate -> candidate.getComponent().getClassName().equals("Owner"));

        boolean hasOwnerController = candidates.stream()
                .anyMatch(candidate -> candidate.getComponent().getClassName().equals("OwnerController"));

        assertTrue(hasOwner);
        assertTrue(hasOwnerController);
    }
}