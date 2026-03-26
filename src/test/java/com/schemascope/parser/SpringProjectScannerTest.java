package com.schemascope.parser;

import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringProjectScannerTest {

    @Test
    void shouldScanCurrentProjectAndFindSpringComponents() throws Exception {
        SpringProjectScanner scanner = new SpringProjectScanner();

        String projectRoot = Paths.get(".").toAbsolutePath().normalize().toString();
        JavaProjectScanResult result = scanner.scan(projectRoot);

        System.out.println(result);

        assertTrue(result.countByType(JavaComponentType.REST_CONTROLLER) >= 1);
        assertTrue(result.countByType(JavaComponentType.SERVICE) >= 1);

        boolean scannerMisclassified = result.getComponents().stream()
                .anyMatch(component ->
                        component.getClassName().equals("SpringProjectScanner")
                                && component.getComponentType() == JavaComponentType.REST_CONTROLLER
                );

        assertFalse(scannerMisclassified);
    }
}