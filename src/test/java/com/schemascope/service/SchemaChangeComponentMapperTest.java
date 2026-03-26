package com.schemascope.service;

import com.schemascope.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaChangeComponentMapperTest {

    @Test
    void shouldMapSchemaChangeToMatchingComponents() {
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

        JavaProjectScanResult scanResult = new JavaProjectScanResult(List.of(
                new JavaComponent("Owner", "/tmp/Owner.java", JavaComponentType.ENTITY),
                new JavaComponent("OwnerController", "/tmp/OwnerController.java", JavaComponentType.CONTROLLER),
                new JavaComponent("Pet", "/tmp/Pet.java", JavaComponentType.ENTITY)
        ));

        SchemaChangeComponentMapper mapper = new SchemaChangeComponentMapper();
        List<ComponentImpactCandidate> candidates = mapper.mapCandidates(change, scanResult);

        assertEquals(2, candidates.size());
        assertEquals("Owner", candidates.get(0).getComponent().getClassName());
        assertEquals("OwnerController", candidates.get(1).getComponent().getClassName());

        assertTrue(candidates.get(0).getScore() > candidates.get(1).getScore());
    }
}