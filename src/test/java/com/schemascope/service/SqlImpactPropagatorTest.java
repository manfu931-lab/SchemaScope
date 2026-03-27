package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlImpactCandidate;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlImpactPropagatorTest {

    @Test
    void shouldPropagateMatchedSqlImpactToServiceAndController() throws Exception {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        SqlAccessExtractor extractor = new SqlAccessExtractor();
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
        List<SqlImpactCandidate> sqlCandidates = matcher.match(change, accessPoints);

        SpringProjectScanner scanner = new SpringProjectScanner();
        JavaProjectScanResult scanResult = scanner.scan(projectRoot.toString());

        SqlImpactPropagator propagator = new SqlImpactPropagator();
        List<ComponentImpactCandidate> componentCandidates = propagator.propagate(sqlCandidates, scanResult);

        System.out.println("SqlImpactPropagator candidates = " + componentCandidates);

        boolean hasOwnerRepository = componentCandidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && candidate.getReason().contains("owns matched SQL access point")
        );

        boolean hasOwnerJdbcDao = componentCandidates.stream().anyMatch(candidate ->
                "OwnerJdbcDao".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && candidate.getReason().contains("owns matched SQL access point")
        );

        boolean hasOwnerService = componentCandidates.stream().anyMatch(candidate ->
                "OwnerService".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && candidate.getReason().contains("OwnerRepository")
        );

        boolean hasOwnerController = componentCandidates.stream().anyMatch(candidate ->
                "OwnerController".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.INDIRECT
                        && candidate.getReason().contains("OwnerService")
        );

        assertTrue(hasOwnerRepository, "Expected OwnerRepository to be a direct SQL owner match");
        assertTrue(hasOwnerJdbcDao, "Expected OwnerJdbcDao to be a direct SQL owner match");
        assertTrue(hasOwnerService, "Expected OwnerService to be propagated from OwnerRepository");
        assertTrue(hasOwnerController, "Expected OwnerController to be propagated from OwnerService");
    }
}