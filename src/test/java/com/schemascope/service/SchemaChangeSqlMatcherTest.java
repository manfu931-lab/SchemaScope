package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlImpactCandidate;
import com.schemascope.parser.MyBatisXmlSqlExtractor;
import com.schemascope.parser.SqlAccessExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaChangeSqlMatcherTest {

    @Test
    void shouldMatchColumnLevelSchemaChangeToSqlAccessPointsIncludingQuotedIdentifiers() throws Exception {
        SqlAccessExtractor extractor = new SqlAccessExtractor(new MyBatisXmlSqlExtractor());
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        List<SqlAccessPoint> accessPoints = extractor.extractFromProject(projectRoot.toString());

        SchemaChange change = new SchemaChange(
                "chg-drop-column-owners-last-name",
                ChangeType.DROP_COLUMN,
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                true,
                "schema-diff"
        );

        SchemaChangeSqlMatcher matcher = new SchemaChangeSqlMatcher();
        List<SqlImpactCandidate> candidates = matcher.match(change, accessPoints);

        System.out.println("SchemaChangeSqlMatcher candidates = " + candidates);

        boolean hasRepositoryMatch = candidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getAccessPoint().getOwnerClassName())
                        && "findByLastName".equals(candidate.getAccessPoint().getOwnerMethodName())
                        && candidate.isTableMatched()
                        && candidate.isColumnMatched()
        );

        boolean hasJdbcMatch = candidates.stream().anyMatch(candidate ->
                "OwnerJdbcDao".equals(candidate.getAccessPoint().getOwnerClassName())
                        && "updateOwnerLastName".equals(candidate.getAccessPoint().getOwnerMethodName())
                        && candidate.isTableMatched()
                        && candidate.isColumnMatched()
        );

        boolean hasQuotedRepositoryMatch = candidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getAccessPoint().getOwnerClassName())
                        && "findQuotedOwnerName".equals(candidate.getAccessPoint().getOwnerMethodName())
                        && candidate.isTableMatched()
                        && candidate.isColumnMatched()
        );

        assertTrue(hasRepositoryMatch, "Expected OwnerRepository.findByLastName to be matched");
        assertTrue(hasJdbcMatch, "Expected OwnerJdbcDao.updateOwnerLastName to be matched");
        assertTrue(hasQuotedRepositoryMatch, "Expected OwnerRepository.findQuotedOwnerName to be matched");
    }

    @Test
    void shouldNotMatchWhenOnlyTableMatchesButColumnDoesNot() throws Exception {
        SqlAccessExtractor extractor = new SqlAccessExtractor(new MyBatisXmlSqlExtractor());
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        List<SqlAccessPoint> accessPoints = extractor.extractFromProject(projectRoot.toString());

        SchemaChange change = new SchemaChange(
                "chg-drop-column-owners-first-name",
                ChangeType.DROP_COLUMN,
                "owners",
                "first_name",
                "VARCHAR(80)",
                null,
                true,
                "schema-diff"
        );

        SchemaChangeSqlMatcher matcher = new SchemaChangeSqlMatcher();
        List<SqlImpactCandidate> candidates = matcher.match(change, accessPoints);

        System.out.println("SchemaChangeSqlMatcher non-match candidates = " + candidates);

        assertTrue(candidates.isEmpty(), "Expected no SQL match when column token does not appear in SQL");
    }

    @Test
    void shouldMatchTableLevelSchemaChangeByTableOnly() throws Exception {
        SqlAccessExtractor extractor = new SqlAccessExtractor(new MyBatisXmlSqlExtractor());
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        List<SqlAccessPoint> accessPoints = extractor.extractFromProject(projectRoot.toString());

        SchemaChange change = new SchemaChange(
                "chg-drop-table-owners",
                ChangeType.DROP_TABLE,
                "owners",
                null,
                null,
                null,
                true,
                "schema-diff"
        );

        SchemaChangeSqlMatcher matcher = new SchemaChangeSqlMatcher();
        List<SqlImpactCandidate> candidates = matcher.match(change, accessPoints);

        System.out.println("SchemaChangeSqlMatcher table-level candidates = " + candidates);

        boolean hasOwnerRepository = candidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getAccessPoint().getOwnerClassName())
        );

        boolean hasOwnerJdbcDao = candidates.stream().anyMatch(candidate ->
                "OwnerJdbcDao".equals(candidate.getAccessPoint().getOwnerClassName())
        );

        boolean hasQuotedOwnerRepository = candidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getAccessPoint().getOwnerClassName())
                        && "findQuotedOwnerName".equals(candidate.getAccessPoint().getOwnerMethodName())
        );

        assertTrue(hasOwnerRepository, "Expected OwnerRepository table-level match");
        assertTrue(hasOwnerJdbcDao, "Expected OwnerJdbcDao table-level match");
        assertTrue(hasQuotedOwnerRepository, "Expected quoted OwnerRepository table-level match");
        assertTrue(candidates.stream().allMatch(SqlImpactCandidate::isTableMatched));
    }
}