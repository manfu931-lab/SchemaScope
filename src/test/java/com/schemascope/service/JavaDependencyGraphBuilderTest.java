package com.schemascope.service;

import com.schemascope.domain.JavaDependencyEdge;
import com.schemascope.domain.JavaDependencyGraph;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.parser.SpringProjectScanner;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaDependencyGraphBuilderTest {

    @Test
    void shouldBuildDependencyGraphWithMethodCallEvidence() throws Exception {
        Path projectRoot = Path.of("src", "test", "resources", "fixture", "sql-demo-project")
                .toAbsolutePath()
                .normalize();

        SpringProjectScanner scanner = new SpringProjectScanner();
        JavaProjectScanResult scanResult = scanner.scan(projectRoot.toString());

        JavaDependencyGraphBuilder builder = new JavaDependencyGraphBuilder();
        JavaDependencyGraph graph = builder.build(scanResult);

        System.out.println("Java dependency graph = " + graph);

        List<JavaDependencyEdge> edges = graph.getEdges();

        boolean hasRepositoryToServiceMethodCall = edges.stream().anyMatch(edge ->
                "OwnerRepository".equals(edge.getDependencyClassName())
                        && "OwnerService".equals(edge.getDependentClassName())
                        && "METHOD_CALL".equals(edge.getEvidenceType())
                        && "searchByLastName".equals(edge.getDependentMethodName())
                        && "findByLastName".equals(edge.getDependencyMethodName())
        );

        boolean hasServiceToControllerMethodCall = edges.stream().anyMatch(edge ->
                "OwnerService".equals(edge.getDependencyClassName())
                        && "OwnerController".equals(edge.getDependentClassName())
                        && "METHOD_CALL".equals(edge.getEvidenceType())
                        && "listOwners".equals(edge.getDependentMethodName())
                        && "searchByLastName".equals(edge.getDependencyMethodName())
        );

        assertTrue(hasRepositoryToServiceMethodCall,
                "Expected OwnerService.searchByLastName -> OwnerRepository.findByLastName");
        assertTrue(hasServiceToControllerMethodCall,
                "Expected OwnerController.listOwners -> OwnerService.searchByLastName");
    }
}