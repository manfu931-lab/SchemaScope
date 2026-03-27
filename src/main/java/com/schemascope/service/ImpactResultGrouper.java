package com.schemascope.service;

import com.schemascope.domain.GroupedImpactResults;
import com.schemascope.domain.ImpactRelationLevel;
import com.schemascope.domain.ImpactResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImpactResultGrouper {

    public GroupedImpactResults group(List<ImpactResult> results) {
        List<ImpactResult> direct = results.stream()
                .filter(result -> result.getRelationLevel() == ImpactRelationLevel.DIRECT)
                .collect(Collectors.toList());

        List<ImpactResult> indirect = results.stream()
                .filter(result -> result.getRelationLevel() == ImpactRelationLevel.INDIRECT)
                .collect(Collectors.toList());

        return new GroupedImpactResults(direct, indirect);
    }
}