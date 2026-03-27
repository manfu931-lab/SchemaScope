package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class ImpactSurfaceSummary {

    private List<ApiEndpointImpact> impactedEndpoints = new ArrayList<>();
    private List<TestImpactHint> suggestedTests = new ArrayList<>();

    public ImpactSurfaceSummary() {
    }

    public ImpactSurfaceSummary(List<ApiEndpointImpact> impactedEndpoints,
                                List<TestImpactHint> suggestedTests) {
        this.impactedEndpoints = impactedEndpoints == null ? new ArrayList<>() : impactedEndpoints;
        this.suggestedTests = suggestedTests == null ? new ArrayList<>() : suggestedTests;
    }

    public List<ApiEndpointImpact> getImpactedEndpoints() {
        return impactedEndpoints;
    }

    public void setImpactedEndpoints(List<ApiEndpointImpact> impactedEndpoints) {
        this.impactedEndpoints = impactedEndpoints == null ? new ArrayList<>() : impactedEndpoints;
    }

    public List<TestImpactHint> getSuggestedTests() {
        return suggestedTests;
    }

    public void setSuggestedTests(List<TestImpactHint> suggestedTests) {
        this.suggestedTests = suggestedTests == null ? new ArrayList<>() : suggestedTests;
    }

    @Override
    public String toString() {
        return "ImpactSurfaceSummary{" +
                "impactedEndpoints=" + impactedEndpoints +
                ", suggestedTests=" + suggestedTests +
                '}';
    }
}