package com.schemascope.service;

import com.schemascope.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EvidenceGraphExporter {

    public EvidenceGraphExport export(AnalysisRequest request, PrReviewReport report) {
        Map<String, EvidenceGraphNode> nodes = new LinkedHashMap<>();
        List<EvidenceGraphEdge> edges = new ArrayList<>();

        if (report == null) {
            return new EvidenceGraphExport(new ArrayList<>(), new ArrayList<>(), "graph TD", "No evidence graph generated.");
        }

        String changeNodeId = "change";
        addNode(nodes, new EvidenceGraphNode(
                changeNodeId,
                report.getChangeSummary(),
                "SCHEMA_CHANGE",
                report.getOverallRiskLevel(),
                ImpactRelationLevel.DIRECT
        ));

        Map<String, String> componentNodeIds = new LinkedHashMap<>();
        Map<String, String> firstPropagationParentByChild = new LinkedHashMap<>();

        List<ImpactResult> results = report.getTopRiskResults() == null ? new ArrayList<>() : report.getTopRiskResults();

        for (ImpactResult result : results) {
            String componentNodeId = "component_" + normalizeId(result.getAffectedObject());
            componentNodeIds.put(result.getAffectedObject(), componentNodeId);

            addNode(nodes, new EvidenceGraphNode(
                    componentNodeId,
                    result.getAffectedObject() + " [" + result.getAffectedType() + "]",
                    result.getAffectedType(),
                    result.getRiskLevel(),
                    result.getRelationLevel()
            ));

            String sqlOwner = extractSqlOwner(result);
            if (sqlOwner != null) {
                String sqlNodeId = "sql_" + normalizeId(sqlOwner);
                addNode(nodes, new EvidenceGraphNode(
                        sqlNodeId,
                        sqlOwner,
                        "SQL_OWNER",
                        result.getRiskLevel(),
                        ImpactRelationLevel.DIRECT
                ));

                addEdge(edges, changeNodeId, sqlNodeId, "MATCHES_SQL", "matches");
                addEdge(edges, sqlNodeId, componentNodeId, "OWNS_SQL", "owns matched SQL");
            } else {
                addEdge(edges, changeNodeId, componentNodeId, "AFFECTS_COMPONENT", "affects");
            }

            String propagationParent = extractPropagationParent(result);
            if (propagationParent != null) {
                firstPropagationParentByChild.putIfAbsent(result.getAffectedObject(), propagationParent);
            }
        }

        for (Map.Entry<String, String> entry : firstPropagationParentByChild.entrySet()) {
            String child = entry.getKey();
            String parent = entry.getValue();

            String parentNodeId = componentNodeIds.get(parent);
            String childNodeId = componentNodeIds.get(child);

            if (parentNodeId != null && childNodeId != null) {
                addEdge(edges, parentNodeId, childNodeId, "PROPAGATES_TO", "propagates");
            }
        }

        if (report.getImpactedEndpoints() != null) {
            for (ApiEndpointImpact endpoint : report.getImpactedEndpoints()) {
                String endpointNodeId = "endpoint_" + normalizeId(endpoint.getHttpMethod() + "_" + endpoint.getPath());
                addNode(nodes, new EvidenceGraphNode(
                        endpointNodeId,
                        endpoint.getHttpMethod() + " " + endpoint.getPath(),
                        "API_ENDPOINT",
                        endpoint.getRiskLevel(),
                        endpoint.getRelationLevel()
                ));

                String controllerNodeId = componentNodeIds.get(endpoint.getOwnerController());
                if (controllerNodeId != null) {
                    addEdge(edges, controllerNodeId, endpointNodeId, "EXPOSES_ENDPOINT", "exposes");
                }
            }
        }

        if (report.getTestExecutionPlan() != null) {
            for (SelectedTestCase testCase : report.getTestExecutionPlan().getPrioritizedExistingTests()) {
                String testNodeId = "test_" + normalizeId(testCase.getTestClassName());
                addNode(nodes, new EvidenceGraphNode(
                        testNodeId,
                        testCase.getTestClassName(),
                        "TEST_CASE",
                        testCase.getPriority(),
                        ImpactRelationLevel.INDIRECT
                ));

                String ownerComponent = inferOwnerComponentFromTestName(testCase.getTestClassName(), componentNodeIds);
                if (ownerComponent != null) {
                    addEdge(edges, componentNodeIds.get(ownerComponent), testNodeId, "COVERED_BY_TEST", "covered by");
                }
            }

            for (SelectedTestCase testCase : report.getTestExecutionPlan().getMissingRecommendedTests()) {
                String testNodeId = "missing_test_" + normalizeId(testCase.getTestClassName());
                addNode(nodes, new EvidenceGraphNode(
                        testNodeId,
                        testCase.getTestClassName(),
                        "MISSING_TEST",
                        testCase.getPriority(),
                        ImpactRelationLevel.INDIRECT
                ));

                String ownerComponent = inferOwnerComponentFromTestName(testCase.getTestClassName(), componentNodeIds);
                if (ownerComponent != null) {
                    addEdge(edges, componentNodeIds.get(ownerComponent), testNodeId, "RECOMMEND_TEST", "recommend test");
                }
            }
        }

        String mermaid = toMermaid(new ArrayList<>(nodes.values()), edges);
        String summary = buildSummary(report, nodes.size(), edges.size());

        return new EvidenceGraphExport(
                new ArrayList<>(nodes.values()),
                edges,
                mermaid,
                summary
        );
    }

    private void addNode(Map<String, EvidenceGraphNode> nodes, EvidenceGraphNode node) {
        nodes.putIfAbsent(node.getNodeId(), node);
    }

    private void addEdge(List<EvidenceGraphEdge> edges,
                         String fromNodeId,
                         String toNodeId,
                         String edgeType,
                         String label) {
        boolean exists = edges.stream().anyMatch(edge ->
                edge.getFromNodeId().equals(fromNodeId)
                        && edge.getToNodeId().equals(toNodeId)
                        && edge.getEdgeType().equals(edgeType)
        );

        if (!exists) {
            edges.add(new EvidenceGraphEdge(fromNodeId, toNodeId, edgeType, label));
        }
    }

    private String extractSqlOwner(ImpactResult result) {
        if (result.getEvidencePath() == null) {
            return null;
        }

        for (String step : result.getEvidencePath()) {
            if (step.startsWith("SQL owner: ")) {
                return step.substring("SQL owner: ".length()).trim();
            }
        }
        return null;
    }

    private String extractPropagationParent(ImpactResult result) {
        if (result.getEvidencePath() == null) {
            return null;
        }

        for (String step : result.getEvidencePath()) {
            if (step.startsWith("Propagation: ")) {
                String payload = step.substring("Propagation: ".length()).trim();
                int idx = payload.indexOf(" references ");
                if (idx > 0) {
                    return payload.substring(idx + " references ".length()).trim();
                }
            }
        }
        return null;
    }

    private String inferOwnerComponentFromTestName(String testClassName, Map<String, String> componentNodeIds) {
        for (String componentName : componentNodeIds.keySet()) {
            if (testClassName.contains(componentName)) {
                return componentName;
            }
        }
        return null;
    }

    private String toMermaid(List<EvidenceGraphNode> nodes, List<EvidenceGraphEdge> edges) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");

        for (EvidenceGraphNode node : nodes) {
            sb.append("    ")
                    .append(node.getNodeId())
                    .append("[\"")
                    .append(escape(node.getLabel()))
                    .append("\"]\n");
        }

        for (EvidenceGraphEdge edge : edges) {
            sb.append("    ")
                    .append(edge.getFromNodeId())
                    .append(" -->|")
                    .append(escape(edge.getLabel()))
                    .append("| ")
                    .append(edge.getToNodeId())
                    .append("\n");
        }

        return sb.toString();
    }

    private String buildSummary(PrReviewReport report, int nodeCount, int edgeCount) {
        return "Graph exported for "
                + report.getChangeSummary()
                + ", with "
                + nodeCount
                + " nodes and "
                + edgeCount
                + " edges.";
    }

    private String normalizeId(String raw) {
        return raw == null ? "unknown" : raw.replaceAll("[^a-zA-Z0-9]+", "_").toLowerCase();
    }

    private String escape(String raw) {
        return raw == null ? "" : raw.replace("\"", "'");
    }
}