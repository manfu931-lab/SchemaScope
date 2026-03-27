package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ComponentImpactResultBuilder {

    public List<ImpactResult> build(SchemaChange change, List<ComponentImpactCandidate> candidates) {
        List<ImpactResult> results = new ArrayList<>();

        for (ComponentImpactCandidate candidate : candidates) {
            double riskScore = candidate.getScore() * 100.0;

            ImpactResult result = new ImpactResult(
                change.getChangeId(),
                candidate.getComponent().getClassName(),
                candidate.getComponent().getComponentType().name(),
                riskScore,
                toRiskLevel(riskScore),
                candidate.getScore(),
                Arrays.asList(
                        change.getTableName() + "." + change.getColumnName(),
                        candidate.getReason(),
                        candidate.getComponent().getClassName()
                ),
                candidate.getRelationLevel()
        );

            results.add(result);
        }

        return results;
    }

    private RiskLevel toRiskLevel(double score) {
        if (score >= 85) {
            return RiskLevel.HIGH;
        }
        if (score >= 70) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}