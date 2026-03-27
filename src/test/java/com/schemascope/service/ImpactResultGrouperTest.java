package com.schemascope.service;

import com.schemascope.domain.GroupedImpactResults;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactResultGrouperTest {

    @Test
    void shouldGroupImpactResultsByRelationLevel() {
        ImpactResultGrouper grouper = new ImpactResultGrouper();

        List<ImpactResult> results = List.of(
                new ImpactResult(
                        "chg-1",
                        "Owner",
                        "ENTITY",
                        95.0,
                        RiskLevel.HIGH,
                        0.95,
                        List.of("owners.last_name", "Owner"),
                        ImpactRelationLevel.DIRECT
                ),
                new ImpactResult(
                        "chg-1",
                        "OwnerController",
                        "CONTROLLER",
                        85.0,
                        RiskLevel.HIGH,
                        0.85,
                        List.of("owners.last_name", "OwnerController"),
                        ImpactRelationLevel.INDIRECT
                )
        );

        GroupedImpactResults grouped = grouper.group(results);

        assertEquals(1, grouped.getDirectResults().size());
        assertEquals(1, grouped.getIndirectResults().size());
        assertEquals("Owner", grouped.getDirectResults().get(0).getAffectedObject());
        assertEquals("OwnerController", grouped.getIndirectResults().get(0).getAffectedObject());
    }
}