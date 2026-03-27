package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import org.springframework.stereotype.Component;
import com.schemascope.domain.ImpactRelationLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class SchemaChangeComponentMapper {

    public List<ComponentImpactCandidate> mapCandidates(SchemaChange change, JavaProjectScanResult scanResult) {
        List<ComponentImpactCandidate> candidates = new ArrayList<>();

        String tableToken = normalizeTableToken(change.getTableName());
        List<String> columnTokens = normalizeColumnTokens(change.getColumnName());

        for (JavaComponent component : scanResult.getComponents()) {
            String className = component.getClassName().toLowerCase();
            String filePath = component.getFilePath().toLowerCase();

            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            boolean tableMatched = false;

            if (!tableToken.isBlank() && className.contains(tableToken)) {
                score += 0.55;
                tableMatched = true;
                reasons.add("class name matches table token '" + tableToken + "'");
            }

            if (!tableToken.isBlank() && filePath.contains(tableToken)) {
                score += 0.15;
                tableMatched = true;
                reasons.add("file path matches table token '" + tableToken + "'");
            }

            double typeBonus = scoreByType(component.getComponentType());
            if (typeBonus > 0) {
                score += typeBonus;
                reasons.add("component type bonus: " + component.getComponentType());
            }

            double columnBonus = scoreByColumnTokens(className, filePath, columnTokens, reasons);
            score += columnBonus;
            boolean columnMatched = columnBonus > 0;

            double structuralBonus = scoreByColumnLevelChange(change, component.getComponentType(), tableMatched);
            if (structuralBonus > 0) {
                score += structuralBonus;
                reasons.add("column-level schema change bonus: " + component.getComponentType());
            }

            score = Math.min(score, 1.0);

            if (score >= 0.70) {
                ImpactRelationLevel relationLevel = resolveRelationLevel(
                    component.getComponentType(),
                    tableMatched,
                    columnMatched
                );

                candidates.add(new ComponentImpactCandidate(
                    component,
                    score,
                    String.join("; ", reasons),
                    relationLevel
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

    private List<String> normalizeColumnTokens(String columnName) {
        List<String> tokens = new ArrayList<>();

        if (columnName == null || columnName.isBlank()) {
            return tokens;
        }

        String normalized = columnName.toLowerCase().trim();
        for (String token : normalized.split("_")) {
            if (!token.isBlank() && token.length() >= 3) {
                tokens.add(token);
            }
        }

        if (tokens.isEmpty() && normalized.length() >= 3) {
            tokens.add(normalized);
        }

        return tokens;
    }

    private double scoreByColumnTokens(String className,
                                       String filePath,
                                       List<String> columnTokens,
                                       List<String> reasons) {
        double bonus = 0.0;

        for (String token : columnTokens) {
            if (className.contains(token) || filePath.contains(token)) {
                bonus += 0.08;
                reasons.add("column token hint '" + token + "'");
            }
        }

        return Math.min(bonus, 0.12);
    }

    private double scoreByColumnLevelChange(SchemaChange change,
                                            JavaComponentType type,
                                            boolean tableMatched) {
        if (!tableMatched) {
            return 0.0;
        }

        if (change.getColumnName() == null || change.getColumnName().isBlank()) {
            return 0.0;
        }

        return switch (type) {
            case ENTITY -> 0.05;
            case REPOSITORY -> 0.04;
            case SERVICE -> 0.03;
            case CONTROLLER, REST_CONTROLLER -> 0.0;
        };
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

    private ImpactRelationLevel resolveRelationLevel(JavaComponentType type,boolean tableMatched,boolean columnMatched) {
        if (!tableMatched) {
            return ImpactRelationLevel.INDIRECT;
        }

        return switch (type) {
            case ENTITY, REPOSITORY -> ImpactRelationLevel.DIRECT;
            case SERVICE -> columnMatched ? ImpactRelationLevel.DIRECT : ImpactRelationLevel.INDIRECT;
            case CONTROLLER, REST_CONTROLLER -> ImpactRelationLevel.INDIRECT;
        };
    }
}