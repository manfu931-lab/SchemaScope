package com.schemascope.domain;

import java.util.ArrayList;
import java.util.List;

public class GroupedImpactCandidates {

    private List<ComponentImpactCandidate> directCandidates = new ArrayList<>();
    private List<ComponentImpactCandidate> indirectCandidates = new ArrayList<>();

    public GroupedImpactCandidates() {
    }

    public GroupedImpactCandidates(List<ComponentImpactCandidate> directCandidates,
                                   List<ComponentImpactCandidate> indirectCandidates) {
        this.directCandidates = directCandidates;
        this.indirectCandidates = indirectCandidates;
    }

    public List<ComponentImpactCandidate> getDirectCandidates() {
        return directCandidates;
    }

    public void setDirectCandidates(List<ComponentImpactCandidate> directCandidates) {
        this.directCandidates = directCandidates;
    }

    public List<ComponentImpactCandidate> getIndirectCandidates() {
        return indirectCandidates;
    }

    public void setIndirectCandidates(List<ComponentImpactCandidate> indirectCandidates) {
        this.indirectCandidates = indirectCandidates;
    }

    @Override
    public String toString() {
        return "GroupedImpactCandidates{" +
                "directCandidates=" + directCandidates +
                ", indirectCandidates=" + indirectCandidates +
                '}';
    }
}