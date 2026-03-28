package com.schemascope.benchmark;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringPetClinicExternalBenchmarkTest {

    @Test
    void shouldRunExternalBenchmarkAgainstSpringPetClinicCheckout() throws Exception {
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

        System.out.println("Spring PetClinic external benchmark suite result = " + suiteResult);
        suiteResult.getCaseResults().forEach(result ->
                System.out.println("Spring PetClinic external case result = " + result)
        );

        assertEquals(2, suiteResult.getCaseResults().size(), "Expected 2 external benchmark cases");

        boolean hasManualCase = suiteResult.getCaseResults().stream()
                .anyMatch(result -> "spring-petclinic-manual-drop-column".equals(result.getCaseId()));

        boolean hasSchemaDiffCase = suiteResult.getCaseResults().stream()
                .anyMatch(result -> "spring-petclinic-schema-diff-drop-column".equals(result.getCaseId()));

        assertTrue(hasManualCase, "Expected manual external benchmark case");
        assertTrue(hasSchemaDiffCase, "Expected schema-diff external benchmark case");

        assertTrue(suiteResult.getAverageRecall() >= 0.80,
                "Expected external benchmark recall >= 0.80, actual=" + suiteResult.getAverageRecall());
        assertTrue(suiteResult.getEvidenceCoverageRate() >= 0.80,
                "Expected external evidence coverage >= 0.80, actual=" + suiteResult.getEvidenceCoverageRate());
        assertTrue(suiteResult.getRelationAccuracyRate() >= 0.80,
                "Expected external relation accuracy >= 0.80, actual=" + suiteResult.getRelationAccuracyRate());

        boolean predictedOwnerRepository = suiteResult.getCaseResults().stream().allMatch(result ->
                result.getPredictedAffectedObjects().contains("OwnerRepository")
        );

        boolean predictedOwnerControllerAtLeastOnce = suiteResult.getCaseResults().stream().anyMatch(result ->
                result.getPredictedAffectedObjects().contains("OwnerController")
        );

        assertTrue(predictedOwnerRepository, "Expected OwnerRepository to appear in both external benchmark cases");
        assertTrue(predictedOwnerControllerAtLeastOnce, "Expected OwnerController to appear in at least one external case");
    }
}