package com.schemascope.service;

import com.schemascope.domain.ComponentImpactCandidate;
import com.schemascope.domain.GroupedImpactCandidates;
import com.schemascope.domain.ImpactRelationLevel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImpactCandidateGrouper {

    public GroupedImpactCandidates group(List<ComponentImpactCandidate> candidates) {
        List<ComponentImpactCandidate> direct = candidates.stream()
                .filter(candidate -> candidate.getRelationLevel() == ImpactRelationLevel.DIRECT)
                .collect(Collectors.toList());

        List<ComponentImpactCandidate> indirect = candidates.stream()
                .filter(candidate -> candidate.getRelationLevel() == ImpactRelationLevel.INDIRECT)
                .collect(Collectors.toList());

        return new GroupedImpactCandidates(direct, indirect);
    }
}