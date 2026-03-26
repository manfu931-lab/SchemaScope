package com.schemascope.service;

import com.schemascope.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentImpactResultBuilderTest {

    @Test
    void shouldBuildImpactResultsFromCandidates() {
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

        List<ComponentImpactCandidate> candidates = List.of(
                new ComponentImpactCandidate(
                        new JavaComponent("Owner", "/tmp/Owner.java", JavaComponentType.ENTITY),
                        0.95,
                        "class name matches table token 'owner'"
                ),
                new ComponentImpactCandidate(
                        new JavaComponent("OwnerController", "/tmp/OwnerController.java", JavaComponentType.CONTROLLER),
                        0.75,
                        "class name matches table token 'owner'"
                )
        );

        ComponentImpactResultBuilder builder = new ComponentImpactResultBuilder();
        List<ImpactResult> results = builder.build(change, candidates);

        assertEquals(2, results.size());
        assertEquals("Owner", results.get(0).getAffectedObject());
        assertEquals("ENTITY", results.get(0).getAffectedType());
        assertEquals(95.0, results.get(0).getRiskScore());
        assertEquals(RiskLevel.HIGH, results.get(0).getRiskLevel());
    }
}