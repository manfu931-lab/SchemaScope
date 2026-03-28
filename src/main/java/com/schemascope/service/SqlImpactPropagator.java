package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.JavaComponent;
import com.schemascope.domain.JavaComponentType;
import com.schemascope.domain.JavaDependencyEdge;
import com.schemascope.domain.JavaDependencyGraph;
import com.schemascope.domain.JavaProjectScanResult;
import com.schemascope.domain.SqlImpactCandidate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@Component
public class SqlImpactPropagator {

    private static final int MAX_DEPTH = 2;

    private final JavaDependencyGraphBuilder javaDependencyGraphBuilder;

    public SqlImpactPropagator() {
        this(new JavaDependencyGraphBuilder());
    }

    public SqlImpactPropagator(JavaDependencyGraphBuilder javaDependencyGraphBuilder) {
        this.javaDependencyGraphBuilder = javaDependencyGraphBuilder;
    }

    public List<ComponentImpactCandidate> propagate(List<SqlImpactCandidate> sqlCandidates,
                                                    JavaProjectScanResult scanResult) throws IOException {
        List<ComponentImpactCandidate> results = new ArrayList<>();

        if (sqlCandidates == null || sqlCandidates.isEmpty() || scanResult == null || scanResult.getComponents() == null) {
            return results;
        }

        Map<String, JavaComponent> componentsByName = indexComponents(scanResult);
        JavaDependencyGraph dependencyGraph = javaDependencyGraphBuilder.build(scanResult);

        Map<String, ComponentImpactCandidate> bestCandidateByClass = new LinkedHashMap<>();
        Queue<PropagationNode> queue = new ArrayDeque<>();
        Set<String> visitedEdges = new LinkedHashSet<>();

        for (SqlImpactCandidate sqlCandidate : sqlCandidates) {
            if (sqlCandidate.getAccessPoint() == null) {
                continue;
            }

            String ownerClassName = sqlCandidate.getAccessPoint().getOwnerClassName();
            if (ownerClassName == null || ownerClassName.isBlank()) {
                continue;
            }

            JavaComponent ownerComponent = componentsByName.get(ownerClassName.toLowerCase());
            if (ownerComponent == null) {
                continue;
            }

            double seedScore = clamp(sqlCandidate.getScore() * 0.95);
            ImpactRelationLevel seedRelation = resolveSeedRelation(ownerComponent.getComponentType());

            List<String> seedEvidencePath = new ArrayList<>();
            seedEvidencePath.add("Matched SQL: " + sqlCandidate.getAccessPoint().getSqlId());
            seedEvidencePath.add("SQL source: " + sqlCandidate.getAccessPoint().getSourceType());
            seedEvidencePath.add("SQL owner: "
                    + sqlCandidate.getAccessPoint().getOwnerClassName()
                    + "." + sqlCandidate.getAccessPoint().getOwnerMethodName());
            seedEvidencePath.add("SQL snippet: " + compactSql(sqlCandidate.getAccessPoint().getRawSql()));
            seedEvidencePath.add("Match reason: " + sqlCandidate.getReason());

            ComponentImpactCandidate seedCandidate = new ComponentImpactCandidate(
                    ownerComponent,
                    seedScore,
                    "owns matched SQL access point '" + sqlCandidate.getAccessPoint().getSqlId()
                            + "'; " + sqlCandidate.getReason(),
                    seedRelation,
                    seedEvidencePath
            );

            upsert(bestCandidateByClass, seedCandidate);

            queue.offer(new PropagationNode(
                    ownerComponent.getClassName(),
                    seedScore,
                    0,
                    ownerComponent.getClassName(),
                    seedEvidencePath
            ));
        }

        while (!queue.isEmpty()) {
            PropagationNode current = queue.poll();

            if (current.depth >= MAX_DEPTH) {
                continue;
            }

            for (JavaDependencyEdge edge : dependencyGraph.findDependentsOf(current.className)) {
                String dependentClassName = edge.getDependentClassName();
                JavaComponent dependent = componentsByName.get(dependentClassName.toLowerCase());
                if (dependent == null) {
                    continue;
                }

                String edgeKey = edge.getDependencyClassName() + "->" + edge.getDependentClassName()
                        + "#" + edge.getEvidenceType()
                        + "#" + safe(edge.getDependentMethodName())
                        + "#" + safe(edge.getDependencyMethodName());

                if (!visitedEdges.add(edgeKey)) {
                    continue;
                }

                double propagatedScore = scoreForNextHop(current.score, current.depth + 1,
                        dependent.getComponentType(), edge.getEvidenceType());

                if (propagatedScore < 0.60) {
                    continue;
                }

                ImpactRelationLevel relationLevel = resolvePropagatedRelation(current.depth + 1, dependent.getComponentType());

                List<String> propagatedEvidencePath = new ArrayList<>(current.evidencePath);
                propagatedEvidencePath.add(buildPropagationStep(current.className, dependent.getClassName(), edge));
                propagatedEvidencePath.add("Dependency evidence: " + edge.getEvidenceText().trim());

                ComponentImpactCandidate candidate = new ComponentImpactCandidate(
                        dependent,
                        propagatedScore,
                        buildReason(current.className, current.rootSqlOwnerClass, edge),
                        relationLevel,
                        propagatedEvidencePath
                );

                upsert(bestCandidateByClass, candidate);

                queue.offer(new PropagationNode(
                        dependent.getClassName(),
                        propagatedScore,
                        current.depth + 1,
                        current.rootSqlOwnerClass,
                        propagatedEvidencePath
                ));
            }
        }

        results.addAll(bestCandidateByClass.values());
        results.sort(Comparator.comparing(ComponentImpactCandidate::getScore).reversed()
                .thenComparing(candidate -> candidate.getComponent().getClassName()));

        return results;
    }

    private Map<String, JavaComponent> indexComponents(JavaProjectScanResult scanResult) {
        Map<String, JavaComponent> map = new LinkedHashMap<>();
        for (JavaComponent component : scanResult.getComponents()) {
            if (component.getClassName() == null) {
                continue;
            }
            map.put(component.getClassName().toLowerCase(), component);
        }
        return map;
    }

    private String buildPropagationStep(String dependencyClassName,
                                        String dependentClassName,
                                        JavaDependencyEdge edge) {
        if ("METHOD_CALL".equals(edge.getEvidenceType())
                && edge.getDependentMethodName() != null
                && edge.getDependencyMethodName() != null) {
            return "Method propagation: "
                    + dependentClassName + "." + edge.getDependentMethodName()
                    + " calls "
                    + dependencyClassName + "." + edge.getDependencyMethodName();
        }

        return "Propagation: " + dependentClassName
                + " depends on " + dependencyClassName
                + " via " + edge.getEvidenceType();
    }

    private String buildReason(String dependencyClassName,
                               String rootSqlOwnerClass,
                               JavaDependencyEdge edge) {
        if ("METHOD_CALL".equals(edge.getEvidenceType())
                && edge.getDependentMethodName() != null
                && edge.getDependencyMethodName() != null) {
            return "method '" + edge.getDependentMethodName()
                    + "' calls '" + dependencyClassName + "." + edge.getDependencyMethodName()
                    + "' which traces back to matched SQL owner '" + rootSqlOwnerClass + "'";
        }

        return "depends on '" + dependencyClassName
                + "' which traces back to matched SQL owner '" + rootSqlOwnerClass + "'";
    }

    private void upsert(Map<String, ComponentImpactCandidate> bestCandidateByClass,
                        ComponentImpactCandidate incoming) {
        String className = incoming.getComponent().getClassName();
        ComponentImpactCandidate existing = bestCandidateByClass.get(className);

        if (existing == null || incoming.getScore() > existing.getScore()) {
            bestCandidateByClass.put(className, incoming);
            return;
        }

        if (Math.abs(incoming.getScore() - existing.getScore()) < 0.0001) {
            mergeIfMissing(existing, incoming.getReason());
            mergeEvidencePath(existing, incoming.getEvidencePath());
        }
    }

    private void mergeIfMissing(ComponentImpactCandidate existing, String incomingReason) {
        if (incomingReason == null || incomingReason.isBlank()) {
            return;
        }

        if (existing.getReason() == null || existing.getReason().isBlank()) {
            existing.setReason(incomingReason);
            return;
        }

        if (!existing.getReason().contains(incomingReason)) {
            existing.setReason(existing.getReason() + "; " + incomingReason);
        }
    }

    private void mergeEvidencePath(ComponentImpactCandidate existing, List<String> incomingEvidencePath) {
        if (incomingEvidencePath == null || incomingEvidencePath.isEmpty()) {
            return;
        }

        List<String> merged = new ArrayList<>(existing.getEvidencePath());
        for (String step : incomingEvidencePath) {
            if (!merged.contains(step)) {
                merged.add(step);
            }
        }
        existing.setEvidencePath(merged);
    }

    private double scoreForNextHop(double parentScore,
                                   int nextDepth,
                                   JavaComponentType type,
                                   String evidenceType) {
        double score = parentScore - 0.15;

        if (type == JavaComponentType.SERVICE) {
            score += 0.05;
        } else if (type == JavaComponentType.CONTROLLER || type == JavaComponentType.REST_CONTROLLER) {
            score -= 0.02;
        }

        if ("METHOD_CALL".equals(evidenceType)) {
            score += 0.04;
        }

        if (nextDepth >= 2) {
            score -= 0.03;
        }

        return clamp(score);
    }

    private ImpactRelationLevel resolveSeedRelation(JavaComponentType type) {
        return switch (type) {
            case ENTITY, REPOSITORY -> ImpactRelationLevel.DIRECT;
            case SERVICE, CONTROLLER, REST_CONTROLLER -> ImpactRelationLevel.INDIRECT;
        };
    }

    private ImpactRelationLevel resolvePropagatedRelation(int depth, JavaComponentType type) {
        if (depth == 1 && type == JavaComponentType.SERVICE) {
            return ImpactRelationLevel.DIRECT;
        }
        return ImpactRelationLevel.INDIRECT;
    }

    private double clamp(double score) {
        if (score < 0.0) {
            return 0.0;
        }
        return Math.min(score, 1.0);
    }

    private String compactSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static class PropagationNode {
        private final String className;
        private final double score;
        private final int depth;
        private final String rootSqlOwnerClass;
        private final List<String> evidencePath;

        private PropagationNode(String className,
                                double score,
                                int depth,
                                String rootSqlOwnerClass,
                                List<String> evidencePath) {
            this.className = className;
            this.score = score;
            this.depth = depth;
            this.rootSqlOwnerClass = rootSqlOwnerClass;
            this.evidencePath = new ArrayList<>(evidencePath);
        }
    }
}