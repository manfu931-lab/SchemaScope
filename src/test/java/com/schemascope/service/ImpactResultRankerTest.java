package com.schemascope.service;

import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImpactResultRankerTest {

    @Test
    void shouldSortByRiskScoreAndLimitResults() {
        ImpactResultRanker ranker = new ImpactResultRanker();

        List<ImpactResult> input = List.of(
                new ImpactResult("chg-1", "OwnerController", "CONTROLLER", 75.0, RiskLevel.MEDIUM, 0.75, List.of()),
                new ImpactResult("chg-1", "Owner", "ENTITY", 95.0, RiskLevel.HIGH, 0.95, List.of()),
                new ImpactResult("chg-1", "OwnerService", "SERVICE", 80.0, RiskLevel.MEDIUM, 0.80, List.of()),
                new ImpactResult("chg-1", "OwnerRepository", "REPOSITORY", 90.0, RiskLevel.HIGH, 0.90, List.of()),
                new ImpactResult("chg-1", "Pet", "ENTITY", 60.0, RiskLevel.LOW, 0.60, List.of()),
                new ImpactResult("chg-1", "Visit", "ENTITY", 50.0, RiskLevel.LOW, 0.50, List.of())
        );

        List<ImpactResult> output = ranker.rank(input, 5);

        assertEquals(5, output.size());
        assertEquals("Owner", output.get(0).getAffectedObject());
        assertEquals("OwnerRepository", output.get(1).getAffectedObject());
        assertEquals("OwnerService", output.get(2).getAffectedObject());
        assertEquals("OwnerController", output.get(3).getAffectedObject());
        assertEquals("Pet", output.get(4).getAffectedObject());
    }
}