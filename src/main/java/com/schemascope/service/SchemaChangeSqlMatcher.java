package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlImpactCandidate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class SchemaChangeSqlMatcher {

    public List<SqlImpactCandidate> match(SchemaChange change, List<SqlAccessPoint> accessPoints) {
        List<SqlImpactCandidate> candidates = new ArrayList<>();

        if (change == null || accessPoints == null || accessPoints.isEmpty()) {
            return candidates;
        }

        Set<String> expectedTableTokens = normalizeTableTokens(change.getTableName());
        Set<String> expectedColumnTokens = normalizeColumnTokens(change.getColumnName());

        for (SqlAccessPoint accessPoint : accessPoints) {
            boolean tableMatched = matchesTable(accessPoint, expectedTableTokens);
            boolean columnMatched = matchesColumn(accessPoint, expectedColumnTokens);

            if (!shouldInclude(change, tableMatched, columnMatched)) {
                continue;
            }

            List<String> reasons = new ArrayList<>();
            double score = 0.0;

            if (tableMatched) {
                score += 0.75;
                reasons.add("SQL references table '" + change.getTableName() + "'");
            }

            if (columnMatched) {
                score += 0.20;
                reasons.add("SQL token matches column '" + change.getColumnName() + "'");
            }

            if (tableMatched && columnMatched) {
                score += 0.05;
                reasons.add("table and column evidence both matched");
            }

            score = Math.min(score, 1.0);

            candidates.add(new SqlImpactCandidate(
                    accessPoint,
                    score,
                    String.join("; ", reasons),
                    tableMatched,
                    columnMatched
            ));
        }

        candidates.sort(
                Comparator.comparing(SqlImpactCandidate::getScore).reversed()
                        .thenComparing(candidate -> candidate.getAccessPoint().getSqlId())
        );

        return candidates;
    }

    private boolean shouldInclude(SchemaChange change, boolean tableMatched, boolean columnMatched) {
        if (!tableMatched) {
            return false;
        }

        if (isTableLevelChange(change)) {
            return true;
        }

        if (change.getColumnName() == null || change.getColumnName().isBlank()) {
            return true;
        }

        return columnMatched;
    }

    private boolean isTableLevelChange(SchemaChange change) {
        return change.getChangeType() == ChangeType.ADD_TABLE
                || change.getChangeType() == ChangeType.DROP_TABLE;
    }

    private boolean matchesTable(SqlAccessPoint accessPoint, Set<String> expectedTableTokens) {
        if (expectedTableTokens.isEmpty()) {
            return false;
        }

        for (String table : accessPoint.getReferencedTables()) {
            String normalized = safeLower(table);
            if (expectedTableTokens.contains(normalized)) {
                return true;
            }
            String singular = singularize(normalized);
            if (expectedTableTokens.contains(singular)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesColumn(SqlAccessPoint accessPoint, Set<String> expectedColumnTokens) {
        if (expectedColumnTokens.isEmpty()) {
            return false;
        }

        Set<String> sqlTokens = new LinkedHashSet<>();
        for (String token : accessPoint.getNormalizedTokens()) {
            sqlTokens.add(safeLower(token));
        }

        for (String token : expectedColumnTokens) {
            if (sqlTokens.contains(token)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> normalizeTableTokens(String tableName) {
        Set<String> tokens = new LinkedHashSet<>();

        String normalized = safeLower(tableName).trim();
        if (normalized.isBlank()) {
            return tokens;
        }

        tokens.add(normalized);
        tokens.add(singularize(normalized));

        return tokens;
    }

    private Set<String> normalizeColumnTokens(String columnName) {
        Set<String> tokens = new LinkedHashSet<>();

        String normalized = safeLower(columnName).trim();
        if (normalized.isBlank()) {
            return tokens;
        }

        tokens.add(normalized);

        String compact = normalized.replace("_", "");
        if (!compact.isBlank()) {
            tokens.add(compact);
        }

        for (String token : normalized.split("_")) {
            if (!token.isBlank() && token.length() >= 2) {
                tokens.add(token);
            }
        }

        return tokens;
    }

    private String singularize(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return "";
        }

        if (tableName.endsWith("ies") && tableName.length() > 3) {
            return tableName.substring(0, tableName.length() - 3) + "y";
        }

        if (tableName.endsWith("s") && tableName.length() > 1) {
            return tableName.substring(0, tableName.length() - 1);
        }

        return tableName;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}