package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import com.schemascope.domain.SqlAccessPoint;
import com.schemascope.domain.SqlImpactCandidate;
import com.schemascope.parser.MyBatisXmlSqlExtractor;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlImpactPropagatorTest {

    @Test
    void shouldPropagateMatchedSqlImpactWithMethodCallEvidence() throws Exception {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        SqlAccessExtractor extractor = new SqlAccessExtractor(new MyBatisXmlSqlExtractor());
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

        SqlImpactPropagator propagator = new SqlImpactPropagator(new JavaDependencyGraphBuilder());
        List<ComponentImpactCandidate> componentCandidates = propagator.propagate(sqlCandidates, scanResult);

        System.out.println("SqlImpactPropagator candidates = " + componentCandidates);

        boolean hasOwnerService = componentCandidates.stream().anyMatch(candidate ->
                "OwnerService".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.DIRECT
                        && candidate.getEvidencePath().stream().anyMatch(step ->
                        step.contains("Method propagation: OwnerService.searchByLastName calls OwnerRepository.findByLastName"))
        );

        boolean hasOwnerController = componentCandidates.stream().anyMatch(candidate ->
                "OwnerController".equals(candidate.getComponent().getClassName())
                        && candidate.getRelationLevel() == ImpactRelationLevel.INDIRECT
                        && candidate.getEvidencePath().stream().anyMatch(step ->
                        step.contains("Method propagation: OwnerController.listOwners calls OwnerService.searchByLastName"))
        );

        boolean hasMethodReason = componentCandidates.stream().anyMatch(candidate ->
                candidate.getReason() != null && candidate.getReason().contains("method '")
        );

        assertTrue(hasOwnerService, "Expected method-call evidence for OwnerService propagation");
        assertTrue(hasOwnerController, "Expected method-call evidence for OwnerController propagation");
        assertTrue(hasMethodReason, "Expected method-level reason text in propagated candidates");
    }
}