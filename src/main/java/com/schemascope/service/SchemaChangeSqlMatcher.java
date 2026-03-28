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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SchemaChangeSqlMatcher {

    private static final Pattern QUALIFIED_COLUMN_PATTERN =
            Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\.([`\"]?[a-zA-Z_][a-zA-Z0-9_]*[`\"]?)");

    private static final Pattern TABLE_ALIAS_PATTERN =
            Pattern.compile("(?i)\\b(from|join)\\s+[`\"]?([a-zA-Z0-9_]+)[`\"]?\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

    public List<SqlImpactCandidate> match(SchemaChange change, List<SqlAccessPoint> accessPoints) {
        List<SqlImpactCandidate> candidates = new ArrayList<>();

        if (change == null || accessPoints == null || accessPoints.isEmpty()) {
            return candidates;
        }

        Set<String> expectedTableTokens = normalizeTableTokens(change.getTableName());
        Set<String> expectedColumnTokens = normalizeColumnTokens(change.getColumnName());

        for (SqlAccessPoint accessPoint : accessPoints) {
            boolean tableMatched = matchesTable(accessPoint, expectedTableTokens);
            boolean columnMatched = matchesColumn(accessPoint, expectedColumnTokens)
                    || matchesQualifiedColumns(accessPoint, expectedTableTokens, expectedColumnTokens);

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
            Set<String> normalizedTableTokens = normalizeTableTokens(table);
            if (containsAny(normalizedTableTokens, expectedTableTokens)) {
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
            sqlTokens.addAll(normalizeColumnTokens(token));
        }

        return containsAny(sqlTokens, expectedColumnTokens);
    }

    private boolean matchesQualifiedColumns(SqlAccessPoint accessPoint,
                                            Set<String> expectedTableTokens,
                                            Set<String> expectedColumnTokens) {
        if (accessPoint.getRawSql() == null || expectedColumnTokens.isEmpty() || expectedTableTokens.isEmpty()) {
            return false;
        }

        List<TableAlias> aliases = extractTableAliases(accessPoint.getRawSql());
        Matcher matcher = QUALIFIED_COLUMN_PATTERN.matcher(accessPoint.getRawSql());

        while (matcher.find()) {
            String qualifier = stripQuotes(matcher.group(1));
            String column = stripQuotes(matcher.group(2));

            boolean qualifierMatchesExpectedTable = containsAny(normalizeTableTokens(qualifier), expectedTableTokens);
            boolean columnMatches = containsAny(normalizeColumnTokens(column), expectedColumnTokens);

            if (qualifierMatchesExpectedTable && columnMatches) {
                return true;
            }

            for (TableAlias alias : aliases) {
                boolean aliasMatched = alias.alias.equalsIgnoreCase(qualifier)
                        || alias.table.equalsIgnoreCase(qualifier);

                boolean tableMatched = containsAny(normalizeTableTokens(alias.table), expectedTableTokens);

                if (aliasMatched && tableMatched && columnMatches) {
                    return true;
                }
            }
        }

        return false;
    }

    private List<TableAlias> extractTableAliases(String rawSql) {
        List<TableAlias> aliases = new ArrayList<>();
        Matcher matcher = TABLE_ALIAS_PATTERN.matcher(rawSql);

        while (matcher.find()) {
            aliases.add(new TableAlias(
                    stripQuotes(matcher.group(2)),
                    stripQuotes(matcher.group(3))
            ));
        }

        return aliases;
    }

    private Set<String> normalizeTableTokens(String tableName) {
        Set<String> tokens = new LinkedHashSet<>();

        String normalized = toSnakeCase(stripQuotes(tableName).trim().toLowerCase());
        if (normalized.isBlank()) {
            return tokens;
        }

        tokens.add(normalized);
        tokens.add(normalized.replace("_", ""));

        String singular = singularize(normalized);
        if (!singular.isBlank()) {
            tokens.add(singular);
            tokens.add(singular.replace("_", ""));
        }

        String plural = pluralize(normalized);
        if (!plural.isBlank()) {
            tokens.add(plural);
            tokens.add(plural.replace("_", ""));
        }

        return tokens;
    }

    private Set<String> normalizeColumnTokens(String columnName) {
        Set<String> tokens = new LinkedHashSet<>();

        String normalized = toSnakeCase(stripQuotes(columnName).trim().toLowerCase());
        if (normalized.isBlank()) {
            return tokens;
        }

        tokens.add(normalized);
        tokens.add(normalized.replace("_", ""));

        return tokens;
    }

    private boolean containsAny(Set<String> left, Set<String> right) {
        for (String item : left) {
            if (right.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private String pluralize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.endsWith("s")) {
            return value;
        }
        if (value.endsWith("y") && value.length() > 1) {
            return value.substring(0, value.length() - 1) + "ies";
        }
        return value + "s";
    }

    private String singularize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        if (value.endsWith("ies") && value.length() > 3) {
            return value.substring(0, value.length() - 3) + "y";
        }

        if (value.endsWith("s") && value.length() > 1) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    private String toSnakeCase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String stripped = stripQuotes(value);
        String withUnderscores = stripped
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('-', '_')
                .toLowerCase();

        return withUnderscores.replaceAll("__+", "_");
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("`", "").replace("\"", "");
    }

    private static class TableAlias {
        private final String table;
        private final String alias;

        private TableAlias(String table, String alias) {
            this.table = table;
            this.alias = alias;
        }
    }
}