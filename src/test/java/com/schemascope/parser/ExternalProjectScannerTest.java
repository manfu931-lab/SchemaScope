package com.schemascope.parser;

import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProjectScannerTest {

    @Test
    void shouldScanLocalFixtureProject() throws Exception {
        SpringProjectScanner scanner = new SpringProjectScanner();

        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        JavaProjectScanResult result = scanner.scan(projectRoot.toString());

        System.out.println(result);

        assertTrue(result.getComponents().size() >= 4);
        assertTrue(result.countByType(JavaComponentType.REPOSITORY) >= 2);
        assertTrue(result.countByType(JavaComponentType.SERVICE) >= 1);
        assertTrue(result.countByType(JavaComponentType.REST_CONTROLLER) >= 1);
    }
}