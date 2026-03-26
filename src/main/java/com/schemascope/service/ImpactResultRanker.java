package com.schemascope.service;

import com.schemascope.domain.ImpactResult;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ImpactResultRanker {

    public List<ImpactResult> rank(List<ImpactResult> results, int limit) {
        return results.stream()
                .sorted(Comparator
                        .comparing(ImpactResult::getRiskScore).reversed()
                        .thenComparing(ImpactResult::getConfidence).reversed()
                        .thenComparing(ImpactResult::getAffectedObject))
                .limit(limit)
                .collect(Collectors.toList());
    }
}