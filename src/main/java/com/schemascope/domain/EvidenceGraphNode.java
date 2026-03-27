package com.schemascope.domain;

public class EvidenceGraphNode {

    private String nodeId;
    private String label;
    private String nodeType;
    private RiskLevel riskLevel;
    private ImpactRelationLevel relationLevel;

    public EvidenceGraphNode() {
    }

    public EvidenceGraphNode(String nodeId,
                             String label,
                             String nodeType,
                             RiskLevel riskLevel,
                             ImpactRelationLevel relationLevel) {
        this.nodeId = nodeId;
        this.label = label;
        this.nodeType = nodeType;
        this.riskLevel = riskLevel;
        this.relationLevel = relationLevel;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public ImpactRelationLevel getRelationLevel() {
        return relationLevel;
    }

    public void setRelationLevel(ImpactRelationLevel relationLevel) {
        this.relationLevel = relationLevel;
    }

    @Override
    public String toString() {
        return "EvidenceGraphNode{" +
                "nodeId='" + nodeId + '\'' +
                ", label='" + label + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", riskLevel=" + riskLevel +
                ", relationLevel=" + relationLevel +
                '}';
    }
}