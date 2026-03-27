package com.schemascope.service;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.ImpactResult;
import com.schemascope.domain.ImpactSurfaceSummary;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImpactSurfaceBuilderTest {

    @Test
    void shouldExtractEndpointAndSuggestedTestsFromFixtureProject() throws Exception {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        MockAnalysisService analysisService = new MockAnalysisService(
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
                "manual-surface"
        );

        List<ImpactResult> results = analysisService.analyze(request);

        ImpactSurfaceBuilder builder = new ImpactSurfaceBuilder();
        ImpactSurfaceSummary summary = builder.build(projectRoot.toString(), results);

        System.out.println("Impact surface summary = " + summary);

        boolean hasOwnersEndpoint = summary.getImpactedEndpoints().stream().anyMatch(endpoint ->
                "OwnerController".equals(endpoint.getOwnerController())
                        && "GET".equals(endpoint.getHttpMethod())
                        && "/owners".equals(endpoint.getPath())
        );

        boolean hasControllerTestHint = summary.getSuggestedTests().stream().anyMatch(hint ->
                "OwnerControllerTest".equals(hint.getTestClassName())
        );

        boolean hasServiceTestHint = summary.getSuggestedTests().stream().anyMatch(hint ->
                "OwnerServiceTest".equals(hint.getTestClassName())
        );

        assertTrue(hasOwnersEndpoint, "Expected GET /owners endpoint to be surfaced");
        assertTrue(hasControllerTestHint, "Expected OwnerControllerTest hint");
        assertTrue(hasServiceTestHint, "Expected OwnerServiceTest hint");
    }
}