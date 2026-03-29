package com.schemascope.presentation;

import com.schemascope.domain.AnalysisRequest;
import com.schemascope.domain.view.ReviewPageView;
import com.schemascope.domain.view.ShowcaseDashboardView;
import com.schemascope.parser.SchemaFileReader;
import com.schemascope.parser.SpringProjectScanner;
import com.schemascope.parser.SqlAccessExtractor;
import com.schemascope.schemadiff.SchemaDiffService;
import com.schemascope.service.*;
import com.schemascope.service.impl.MockAnalysisService;
import com.schemascope.service.impl.MockDefenseShowcaseService;
import com.schemascope.service.impl.MockPrReviewService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PresentationAssetJsonExporterTest {

    @Test
    void shouldExportReviewAndShowcasePresentationAssetsToJson() throws Exception {
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

        MockPrReviewService prReviewService = new MockPrReviewService(
                analysisService,
                new ImpactResultGrouper(),
                new PrReviewReportBuilder(),
                new ImpactSurfaceBuilder(),
                new TestImpactPlanner(),
                new EvidenceGraphExporter()
        );

        MockDefenseShowcaseService defenseShowcaseService = new MockDefenseShowcaseService(
                prReviewService,
                new DefenseShowcaseBuilder()
        );

        PresentationAssetAssembler assembler = new PresentationAssetAssembler(
                prReviewService,
                defenseShowcaseService,
                new PresentationViewAssembler()
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
                "presentation-export"
        );

        ReviewPageView reviewPage = assembler.buildReviewPage(request);
        ShowcaseDashboardView showcaseDashboard = assembler.buildShowcaseDashboard(request);

        PresentationAssetJsonExporter exporter = new PresentationAssetJsonExporter();

        Path reviewFile = Path.of("target", "presentation-assets", "review-page.json");
        Path showcaseFile = Path.of("target", "presentation-assets", "showcase-dashboard.json");

        exporter.export(reviewPage, reviewFile);
        exporter.export(showcaseDashboard, showcaseFile);

        assertTrue(Files.exists(reviewFile));
        assertTrue(Files.exists(showcaseFile));
        assertTrue(Files.size(reviewFile) > 0);
        assertTrue(Files.size(showcaseFile) > 0);

        String reviewJson = Files.readString(reviewFile);
        String showcaseJson = Files.readString(showcaseFile);

        assertTrue(reviewJson.contains("SchemaScope PR Review"));
        assertTrue(reviewJson.contains("metricCards"));
        assertTrue(reviewJson.contains("topImpactCards"));

        assertTrue(showcaseJson.contains("SchemaScope"));
        assertTrue(showcaseJson.contains("reviewPage"));
        assertTrue(showcaseJson.contains("demoSteps"));
    }
}