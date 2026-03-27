package com.schemascope.service;

import com.schemascope.domain.ChangeType;
import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SchemaChange;
import com.schemascope.parser.SpringProjectScanner;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProjectMappingTest {

    @Test
    void shouldMapOwnerTableChangeToLocalFixtureComponents() throws Exception {
        SpringProjectScanner scanner = new SpringProjectScanner();
        SchemaChangeComponentMapper mapper = new SchemaChangeComponentMapper();

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        JavaProjectScanResult scanResult = scanner.scan(projectRoot.toString());

        SchemaChange change = new SchemaChange(
                "chg-drop-column-owners-last-name",
                ChangeType.DROP_COLUMN,
                "owners",
                "last_name",
                "last_name VARCHAR(80)",
                null,
                true,
                "schema-diff"
        );

        List<ComponentImpactCandidate> candidates = mapper.mapCandidates(change, scanResult);

        System.out.println("ExternalProjectMappingTest candidates = " + candidates);

        assertFalse(candidates.isEmpty(), "Expected heuristic mapper to return some candidates");

        boolean hasRepositoryLayerMatch = candidates.stream().anyMatch(candidate ->
                "OwnerRepository".equals(candidate.getComponent().getClassName())
                        || "OwnerJdbcDao".equals(candidate.getComponent().getClassName())
        );

        boolean allHaveReason = candidates.stream().allMatch(candidate ->
                candidate.getReason() != null && !candidate.getReason().isBlank()
        );

        boolean allHaveRelationLevel = candidates.stream().allMatch(candidate ->
                candidate.getRelationLevel() != null
        );

        boolean allMeetThreshold = candidates.stream().allMatch(candidate ->
                candidate.getScore() >= 0.70
        );

        assertTrue(hasRepositoryLayerMatch, "Expected at least one repository-layer fallback candidate");
        assertTrue(allHaveReason, "Expected heuristic candidates to carry reasons");
        assertTrue(allHaveRelationLevel, "Expected heuristic candidates to carry relation levels");
        assertTrue(allMeetThreshold, "Expected heuristic candidates to satisfy mapper threshold");
    }
}