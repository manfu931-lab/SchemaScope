package com.schemascope.service;

import com.schemascope.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactCandidateGrouperTest {

    @Test
    void shouldGroupCandidatesByRelationLevel() {
        ImpactCandidateGrouper grouper = new ImpactCandidateGrouper();

        List<ComponentImpactCandidate> candidates = List.of(
                new ComponentImpactCandidate(
                        new JavaComponent("Owner", "/tmp/Owner.java", JavaComponentType.ENTITY),
                        1.0,
                        "entity match",
                        ImpactRelationLevel.DIRECT
                ),
                new ComponentImpactCandidate(
                        new JavaComponent("OwnerController", "/tmp/OwnerController.java", JavaComponentType.CONTROLLER),
                        0.85,
                        "controller match",
                        ImpactRelationLevel.INDIRECT
                )
        );

        GroupedImpactCandidates grouped = grouper.group(candidates);

        assertEquals(1, grouped.getDirectCandidates().size());
        assertEquals(1, grouped.getIndirectCandidates().size());
        assertEquals("Owner", grouped.getDirectCandidates().get(0).getComponent().getClassName());
        assertEquals("OwnerController", grouped.getIndirectCandidates().get(0).getComponent().getClassName());
    }
}