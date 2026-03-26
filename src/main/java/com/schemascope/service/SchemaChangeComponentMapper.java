package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class SchemaChangeComponentMapper {

    public List<ComponentImpactCandidate> mapCandidates(SchemaChange change, JavaProjectScanResult scanResult) {
        List<ComponentImpactCandidate> candidates = new ArrayList<>();

        String tableToken = normalizeTableToken(change.getTableName());
        String columnToken = normalizeColumnToken(change.getColumnName());

        for (JavaComponent component : scanResult.getComponents()) {
            String className = component.getClassName().toLowerCase();
            String filePath = component.getFilePath().toLowerCase();

            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            if (!tableToken.isBlank() && className.contains(tableToken)) {
                score += 0.60;
                reasons.add("class name matches table token '" + tableToken + "'");
            }

            if (!tableToken.isBlank() && filePath.contains(tableToken)) {
                score += 0.15;
                reasons.add("file path matches table token '" + tableToken + "'");
            }

            double typeBonus = scoreByType(component.getComponentType());
            if (typeBonus > 0) {
                score += typeBonus;
                reasons.add("component type bonus: " + component.getComponentType());
            }

            if (!columnToken.isBlank() && (className.contains(columnToken) || filePath.contains(columnToken))) {
                score += 0.05;
                reasons.add("column token hint '" + columnToken + "'");
            }

            score = Math.min(score, 1.0);

            if (score >= 0.70) {
                candidates.add(new ComponentImpactCandidate(
                        component,
                        score,
                        String.join("; ", reasons)
                ));
            }
        }

        candidates.sort(Comparator.comparing(ComponentImpactCandidate::getScore).reversed());
        return candidates;
    }

    private String normalizeTableToken(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "";
        }

        String token = tableName.trim().toLowerCase();

        if (token.endsWith("ies") && token.length() > 3) {
            return token.substring(0, token.length() - 3) + "y";
        }

        if (token.endsWith("s") && token.length() > 1) {
            return token.substring(0, token.length() - 1);
        }

        return token;
    }

    private String normalizeColumnToken(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            return "";
        }

        return columnName.toLowerCase().replace("_", "");
    }

    private double scoreByType(JavaComponentType type) {
        return switch (type) {
            case ENTITY -> 0.25;
            case REPOSITORY -> 0.20;
            case SERVICE -> 0.15;
            case CONTROLLER -> 0.10;
            case REST_CONTROLLER -> 0.10;
        };
    }
}