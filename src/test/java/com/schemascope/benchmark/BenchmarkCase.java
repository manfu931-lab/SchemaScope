package com.schemascope.benchmark;

import com.schemascope.domain.AnalysisRequest;

import java.util.LinkedHashSet;
import java.util.Set;

public class BenchmarkCase {

    private final String caseId;
    private final String description;
    private final AnalysisRequest request;
    private final Set<String> expectedAffectedObjects;
    private final Set<String> expectedDirectObjects;
    private final Set<String> expectedIndirectObjects;
    private final boolean requireEvidencePath;

    public BenchmarkCase(String caseId,
                         String description,
                         AnalysisRequest request,
                         Set<String> expectedAffectedObjects,
                         Set<String> expectedDirectObjects,
                         Set<String> expectedIndirectObjects,
                         boolean requireEvidencePath) {
        this.caseId = caseId;
        this.description = description;
        this.request = request;
        this.expectedAffectedObjects = expectedAffectedObjects == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(expectedAffectedObjects);
        this.expectedDirectObjects = expectedDirectObjects == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(expectedDirectObjects);
        this.expectedIndirectObjects = expectedIndirectObjects == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(expectedIndirectObjects);
        this.requireEvidencePath = requireEvidencePath;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getDescription() {
        return description;
    }

    public AnalysisRequest getRequest() {
        return request;
    }

    public Set<String> getExpectedAffectedObjects() {
        return expectedAffectedObjects;
    }

    public Set<String> getExpectedDirectObjects() {
        return expectedDirectObjects;
    }

    public Set<String> getExpectedIndirectObjects() {
        return expectedIndirectObjects;
    }

    public boolean isRequireEvidencePath() {
        return requireEvidencePath;
    }

    @Override
    public String toString() {
        return "BenchmarkCase{" +
                "caseId='" + caseId + '\'' +
                ", description='" + description + '\'' +
                ", request=" + request +
                ", expectedAffectedObjects=" + expectedAffectedObjects +
                ", expectedDirectObjects=" + expectedDirectObjects +
                ", expectedIndirectObjects=" + expectedIndirectObjects +
                ", requireEvidencePath=" + requireEvidencePath +
                '}';
    }
}