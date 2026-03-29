package com.schemascope.benchmark;

import com.schemascope.benchmark.view.BenchmarkDashboardView;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkDashboardAssemblerTest {

    @Test
    void shouldBuildExternalPetClinicBenchmarkDashboardView() throws Exception {
        Path projectRoot = ExternalProjectBenchmarkSupport.requireProjectRoot(
                "schemascope.external.petclinic.root",
                "SCHEMASCOPE_EXTERNAL_PETCLINIC_ROOT"
        );

        Path oldSchema = ExternalProjectBenchmarkSupport.resolvePetClinicSchema(projectRoot);
        Path newSchema = ExternalProjectBenchmarkSupport.createDroppedColumnVariant(
                oldSchema,
                "owners",
                "last_name"
        );

        List<BenchmarkCase> cases = List.of(
                ExternalProjectBenchmarkSupport.buildManualDropColumnCase(projectRoot),
                ExternalProjectBenchmarkSupport.buildSchemaDiffDropColumnCase(projectRoot, oldSchema, newSchema)
        );

        SchemaScopeBenchmarkRunner runner = new SchemaScopeBenchmarkRunner();
        BenchmarkSuiteResult suiteResult = runner.runAll(cases);

        BenchmarkDashboardAssembler assembler = new BenchmarkDashboardAssembler();
        BenchmarkDashboardView view = assembler.toDashboardView(
                "PetClinic External Benchmark Dashboard",
                suiteResult
        );

        System.out.println("Benchmark dashboard view = " + view);

        assertNotNull(view);
        assertEquals("PetClinic External Benchmark Dashboard", view.getTitle());
        assertNotNull(view.getSummary());
        assertFalse(view.getSummary().isBlank());

        assertNotNull(view.getMetricCards());
        assertTrue(view.getMetricCards().size() >= 5);

        assertNotNull(view.getCaseViews());
        assertEquals(2, view.getCaseViews().size());

        assertNotNull(view.getHighlights());
        assertFalse(view.getHighlights().isEmpty());

        assertTrue(view.getMetricCards().stream()
                .anyMatch(card -> "Average Recall".equals(card.getLabel())));

        assertTrue(view.getCaseViews().stream()
                .anyMatch(caseView -> caseView.getCaseId().contains("spring-petclinic")));
    }
}