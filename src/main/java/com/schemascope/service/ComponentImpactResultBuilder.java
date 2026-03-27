package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.RiskLevel;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
                    buildEvidencePath(change, candidate),
                    candidate.getRelationLevel()
            );

            results.add(result);
        }

        return results;
    }

    private List<String> buildEvidencePath(SchemaChange change, ComponentImpactCandidate candidate) {
        List<String> evidencePath = new ArrayList<>();

        evidencePath.add("Schema change: " + describeChange(change));

        if (candidate.getEvidencePath() != null && !candidate.getEvidencePath().isEmpty()) {
            evidencePath.addAll(candidate.getEvidencePath());
        } else {
            evidencePath.add("Match reason: " + candidate.getReason());
        }

        evidencePath.add("Affected component: " + candidate.getComponent().getClassName());
        evidencePath.add("Relation level: " + candidate.getRelationLevel().name());

        return evidencePath;
    }

    private String describeChange(SchemaChange change) {
        String changeType = change.getChangeType() == null ? "UNKNOWN_CHANGE" : change.getChangeType().name();
        String tableName = change.getTableName() == null ? "unknown_table" : change.getTableName();

        if (change.getColumnName() == null || change.getColumnName().isBlank()) {
            return changeType + " " + tableName;
        }

        return changeType + " " + tableName + "." + change.getColumnName();
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