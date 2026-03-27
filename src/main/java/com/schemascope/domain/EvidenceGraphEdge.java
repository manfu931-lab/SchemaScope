package com.schemascope.domain;

public class EvidenceGraphEdge {

    private String fromNodeId;
    private String toNodeId;
    private String edgeType;
    private String label;

    public EvidenceGraphEdge() {
    }

    public EvidenceGraphEdge(String fromNodeId,
                             String toNodeId,
                             String edgeType,
                             String label) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
        this.edgeType = edgeType;
        this.label = label;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "EvidenceGraphEdge{" +
                "fromNodeId='" + fromNodeId + '\'' +
                ", toNodeId='" + toNodeId + '\'' +
                ", edgeType='" + edgeType + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}