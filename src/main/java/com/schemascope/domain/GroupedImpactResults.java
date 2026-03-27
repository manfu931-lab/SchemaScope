package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class GroupedImpactResults {

    private List<ImpactResult> directResults = new ArrayList<>();
    private List<ImpactResult> indirectResults = new ArrayList<>();

    public GroupedImpactResults() {
    }

    public GroupedImpactResults(List<ImpactResult> directResults, List<ImpactResult> indirectResults) {
        this.directResults = directResults;
        this.indirectResults = indirectResults;
    }

    public List<ImpactResult> getDirectResults() {
        return directResults;
    }

    public void setDirectResults(List<ImpactResult> directResults) {
        this.directResults = directResults;
    }

    public List<ImpactResult> getIndirectResults() {
        return indirectResults;
    }

    public void setIndirectResults(List<ImpactResult> indirectResults) {
        this.indirectResults = indirectResults;
    }

    @Override
    public String toString() {
        return "GroupedImpactResults{" +
                "directResults=" + directResults +
                ", indirectResults=" + indirectResults +
                '}';
    }
}