package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class EvidenceGraphExport {

    private List<EvidenceGraphNode> nodes = new ArrayList<>();
    private List<EvidenceGraphEdge> edges = new ArrayList<>();
    private String mermaid;
    private String summary;

    public EvidenceGraphExport() {
    }

    public EvidenceGraphExport(List<EvidenceGraphNode> nodes,
                               List<EvidenceGraphEdge> edges,
                               String mermaid,
                               String summary) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
        this.edges = edges == null ? new ArrayList<>() : edges;
        this.mermaid = mermaid;
        this.summary = summary;
    }

    public List<EvidenceGraphNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<EvidenceGraphNode> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<EvidenceGraphEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<EvidenceGraphEdge> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }

    public String getMermaid() {
        return mermaid;
    }

    public void setMermaid(String mermaid) {
        this.mermaid = mermaid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "EvidenceGraphExport{" +
                "nodes=" + nodes +
                ", edges=" + edges +
                ", mermaid='" + mermaid + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}