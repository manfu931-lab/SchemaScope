package com.schemascope.domain;

public class ApiEndpointImpact {

    private String ownerController;
    private String httpMethod;
    private String path;
    private RiskLevel riskLevel;
    private ImpactRelationLevel relationLevel;

    public ApiEndpointImpact() {
    }

    public ApiEndpointImpact(String ownerController,
                             String httpMethod,
                             String path,
                             RiskLevel riskLevel,
                             ImpactRelationLevel relationLevel) {
        this.ownerController = ownerController;
        this.httpMethod = httpMethod;
        this.path = path;
        this.riskLevel = riskLevel;
        this.relationLevel = relationLevel;
    }

    public String getOwnerController() {
        return ownerController;
    }

    public void setOwnerController(String ownerController) {
        this.ownerController = ownerController;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        return "ApiEndpointImpact{" +
                "ownerController='" + ownerController + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", path='" + path + '\'' +
                ", riskLevel=" + riskLevel +
                ", relationLevel=" + relationLevel +
                '}';
    }
}