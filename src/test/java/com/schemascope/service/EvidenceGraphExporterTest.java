package com.schemascope.service;

import com.schemascope.domain.*;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.impl.MockAnalysisService;
import com.schemascope.service.impl.MockPrReviewService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenceGraphExporterTest {

    @Test
    void shouldExportEvidenceGraphWithSchemaSqlComponentEndpointAndTestNodes() {
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

        MockPrReviewService reviewService = new MockPrReviewService(
                analysisService,
                new ImpactResultGrouper(),
                new PrReviewReportBuilder(),
                new ImpactSurfaceBuilder(),
                new TestImpactPlanner(),
                new EvidenceGraphExporter()
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
                "manual-graph"
        );

        PrReviewReport report = reviewService.review(request);
        EvidenceGraphExport graph = report.getEvidenceGraph();

        System.out.println("Evidence graph export = " + graph);

        assertTrue(graph != null);
        assertTrue(graph.getNodes().stream().anyMatch(node ->
                "SCHEMA_CHANGE".equals(node.getNodeType())));
        assertTrue(graph.getNodes().stream().anyMatch(node ->
                "SQL_OWNER".equals(node.getNodeType())));
        assertTrue(graph.getNodes().stream().anyMatch(node ->
                "REST_CONTROLLER".equals(node.getNodeType())));
        assertTrue(graph.getNodes().stream().anyMatch(node ->
                "API_ENDPOINT".equals(node.getNodeType())));
        assertTrue(graph.getEdges().stream().anyMatch(edge ->
                "PROPAGATES_TO".equals(edge.getEdgeType())));
        assertTrue(graph.getMermaid().contains("graph TD"));
        assertTrue(graph.getMermaid().contains("OwnerController"));
    }
}