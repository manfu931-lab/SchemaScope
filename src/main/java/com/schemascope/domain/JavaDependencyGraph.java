package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class JavaDependencyGraph {

    private List<JavaDependencyEdge> edges = new ArrayList<>();

    public JavaDependencyGraph() {
    }

    public JavaDependencyGraph(List<JavaDependencyEdge> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }

    public List<JavaDependencyEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<JavaDependencyEdge> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }

    public List<JavaDependencyEdge> findDependentsOf(String dependencyClassName) {
        List<JavaDependencyEdge> result = new ArrayList<>();
        if (dependencyClassName == null) {
            return result;
        }

        for (JavaDependencyEdge edge : edges) {
            if (dependencyClassName.equals(edge.getDependencyClassName())) {
                result.add(edge);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "JavaDependencyGraph{" +
                "edges=" + edges +
                '}';
    }
}