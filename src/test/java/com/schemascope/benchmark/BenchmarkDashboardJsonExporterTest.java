package com.schemascope.benchmark;

import com.schemascope.benchmark.view.BenchmarkDashboardView;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkDashboardJsonExporterTest {

    @Test
    void shouldExportExternalPetClinicBenchmarkDashboardToJson() throws Exception {
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
        BenchmarkDashboardView dashboardView = assembler.toDashboardView(
                "PetClinic External Benchmark Dashboard",
                suiteResult
        );

        BenchmarkDashboardJsonExporter exporter = new BenchmarkDashboardJsonExporter();
        Path outputFile = Path.of("target", "benchmark-dashboard", "petclinic-external-dashboard.json");

        Path exported = exporter.export(dashboardView, outputFile);

        System.out.println("Benchmark dashboard exported to = " + exported.toAbsolutePath());

        assertNotNull(exported);
        assertTrue(Files.exists(exported));
        assertTrue(Files.size(exported) > 0);

        String json = Files.readString(exported);
        assertTrue(json.contains("PetClinic External Benchmark Dashboard"));
        assertTrue(json.contains("Average Recall"));
        assertTrue(json.contains("spring-petclinic"));
        assertTrue(json.contains("metricCards"));
        assertTrue(json.contains("caseViews"));
        assertTrue(json.contains("highlights"));
    }
}