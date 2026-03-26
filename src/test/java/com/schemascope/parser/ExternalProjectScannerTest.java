package com.schemascope.parser;

import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaProjectScanResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalProjectScannerTest {

    @Test
    void shouldScanSpringPetclinicProject() throws Exception {
        SpringProjectScanner scanner = new SpringProjectScanner();

        String projectRoot = "D:/download/SchemaScope/benchmark/spring-petclinic";
        JavaProjectScanResult result = scanner.scan(projectRoot);

        System.out.println(result);

        assertTrue(result.getComponents().size() > 0);
        assertTrue(
                result.countByType(JavaComponentType.REST_CONTROLLER) >= 1
                        || result.countByType(JavaComponentType.CONTROLLER) >= 1
                        || result.countByType(JavaComponentType.SERVICE) >= 1
                        || result.countByType(JavaComponentType.REPOSITORY) >= 1
                        || result.countByType(JavaComponentType.ENTITY) >= 1
        );
    }
}