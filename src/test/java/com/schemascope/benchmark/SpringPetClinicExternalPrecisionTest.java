package com.schemascope.benchmark;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringPetClinicExternalPrecisionTest {

    @Test
    void shouldKeepExternalPetClinicFalsePositivesUnderControl() {
        Path projectRoot = ExternalProjectBenchmarkSupport.requireProjectRoot(
                "schemascope.external.petclinic.root",
                "SCHEMASCOPE_EXTERNAL_PETCLINIC_ROOT"
        );

        Path oldSchema = ExternalProjectBenchmarkSupport.resolvePetClinicSchema(projectRoot);
        Path newSchema;
        try {
            newSchema = ExternalProjectBenchmarkSupport.createDroppedColumnVariant(
                    oldSchema,
                    "owners",
                    "last_name"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<BenchmarkCase> cases = List.of(
                ExternalProjectBenchmarkSupport.buildManualDropColumnCase(projectRoot),
                ExternalProjectBenchmarkSupport.buildSchemaDiffDropColumnCase(projectRoot, oldSchema, newSchema)
        );

        SchemaScopeBenchmarkRunner runner = new SchemaScopeBenchmarkRunner();
        BenchmarkSuiteResult suiteResult = runner.runAll(cases);

        System.out.println("Spring PetClinic external precision suite result = " + suiteResult);

        assertTrue(suiteResult.getAverageRecall() >= 0.99,
                "Expected external recall near 1.0, actual=" + suiteResult.getAverageRecall());

        assertTrue(suiteResult.getEvidenceCoverageRate() >= 0.99,
                "Expected external evidence coverage near 1.0, actual=" + suiteResult.getEvidenceCoverageRate());

        assertTrue(suiteResult.getAveragePrecision() >= 0.60,
                "Expected external precision >= 0.60, actual=" + suiteResult.getAveragePrecision());

        for (BenchmarkCaseResult caseResult : suiteResult.getCaseResults()) {
            long unrelatedControllerCount = caseResult.getPredictedAffectedObjects().stream()
                    .filter(name -> name.endsWith("Controller"))
                    .filter(name -> !name.equals("OwnerController"))
                    .count();

            assertTrue(unrelatedControllerCount <= 1,
                    "Too many unrelated controllers leaked into result: " + caseResult.getPredictedAffectedObjects());
        }
    }
}