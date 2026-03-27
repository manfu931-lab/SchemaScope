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
                new JavaComponent("Owner", "/tmp/owner/Owner.java", JavaComponentType.ENTITY),
                new JavaComponent("OwnerController", "/tmp/owner/OwnerController.java", JavaComponentType.CONTROLLER),
                new JavaComponent("Pet", "/tmp/pet/Pet.java", JavaComponentType.ENTITY)
        ));

        SchemaChangeComponentMapper mapper = new SchemaChangeComponentMapper();
        List<ComponentImpactCandidate> candidates = mapper.mapCandidates(change, scanResult);

        assertEquals(2, candidates.size());
        assertEquals("Owner", candidates.get(0).getComponent().getClassName());
        assertEquals("OwnerController", candidates.get(1).getComponent().getClassName());

        assertTrue(candidates.get(0).getScore() > candidates.get(1).getScore());
        assertTrue(candidates.get(0).getReason().contains("table token"));
        assertTrue(candidates.get(1).getReason().contains("component type bonus"));
    }

    @Test
    void shouldUseColumnTokenToBoostRelevantComponent() {
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
                new JavaComponent("Owner", "/tmp/owner/Owner.java", JavaComponentType.ENTITY),
                new JavaComponent("OwnerNameService", "/tmp/owner/OwnerNameService.java", JavaComponentType.SERVICE),
                new JavaComponent("OwnerController", "/tmp/owner/OwnerController.java", JavaComponentType.CONTROLLER)
        ));

        SchemaChangeComponentMapper mapper = new SchemaChangeComponentMapper();
        List<ComponentImpactCandidate> candidates = mapper.mapCandidates(change, scanResult);

        assertEquals("Owner", candidates.get(0).getComponent().getClassName());
        assertEquals(ImpactRelationLevel.DIRECT, candidates.get(0).getRelationLevel());

        assertEquals("OwnerNameService", candidates.get(1).getComponent().getClassName());
        assertEquals(ImpactRelationLevel.DIRECT, candidates.get(1).getRelationLevel());

        assertEquals("OwnerController", candidates.get(2).getComponent().getClassName());
        assertEquals(ImpactRelationLevel.INDIRECT, candidates.get(2).getRelationLevel());

        assertTrue(candidates.get(1).getReason().contains("column token hint"));
}
}