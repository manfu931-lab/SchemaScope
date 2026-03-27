package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.GroupedImpactResults;
import com.schemascope.domain.ImpactResult;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupedExternalAnalysisTest {

    @Test
    void shouldGroupEvidenceDrivenResultsForLocalFixtureProject() {
        MockAnalysisService service = new MockAnalysisService(
                new SimpleImpactAnalyzer(),
                new SchemaChangeFactory(),
                new SchemaFileReader(),
                new SchemaDiffService(),
                new SpringProjectScanner(),
                new SchemaChangeComponentMapper(),
                new ComponentImpactResultBuilder(),
                new ImpactResultRanker(),
                new SqlAccessExtractor(),
                new SchemaChangeSqlMatcher(),
                new SqlImpactPropagator()
        );

        ImpactResultGrouper grouper = new ImpactResultGrouper();

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        AnalysisRequest request = new AnalysisRequest(
                "sql-demo-project",
                projectRoot.toString(),
                null,
                null,
                "DROP_COLUMN",
                "owners",
                "last_name",
                "VARCHAR(80)",
                null,
                "manual-test"
        );

        List<ImpactResult> results = service.analyze(request);
        GroupedImpactResults grouped = grouper.group(results);

        System.out.println(grouped);

        assertFalse(grouped.getDirectResults().isEmpty());
        assertFalse(grouped.getIndirectResults().isEmpty());

        boolean hasRepositoryInDirect = grouped.getDirectResults().stream()
                .anyMatch(r -> "OwnerRepository".equals(r.getAffectedObject())
                        || "OwnerJdbcDao".equals(r.getAffectedObject()));

        boolean hasOwnerControllerInIndirect = grouped.getIndirectResults().stream()
                .anyMatch(r -> "OwnerController".equals(r.getAffectedObject()));

        assertTrue(hasRepositoryInDirect);
        assertTrue(hasOwnerControllerInIndirect);
    }
}