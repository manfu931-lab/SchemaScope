package com.schemascope.benchmark;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BenchmarkProjectSpec {

    private final String projectId;
    private final Path projectRoot;
    private final Path oldSchema;
    private final Path newSchema;
    private final String tableName;
    private final String columnName;
    private final Set<String> expectedAffectedObjects;
    private final Set<String> expectedDirectObjects;
    private final Set<String> expectedIndirectObjects;

    public BenchmarkProjectSpec(String projectId,
                                Path projectRoot,
                                Path oldSchema,
                                Path newSchema,
                                String tableName,
                                String columnName,
                                Set<String> expectedAffectedObjects,
                                Set<String> expectedDirectObjects,
                                Set<String> expectedIndirectObjects) {
        this.projectId = projectId;
        this.projectRoot = projectRoot;
        this.oldSchema = oldSchema;
        this.newSchema = newSchema;
        this.tableName = tableName;
        this.columnName = columnName;
        this.expectedAffectedObjects = expectedAffectedObjects == null ? new LinkedHashSet<>() : expectedAffectedObjects;
        this.expectedDirectObjects = expectedDirectObjects == null ? new LinkedHashSet<>() : expectedDirectObjects;
        this.expectedIndirectObjects = expectedIndirectObjects == null ? new LinkedHashSet<>() : expectedIndirectObjects;
    }

    public String getProjectId() {
        return projectId;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public Path getOldSchema() {
        return oldSchema;
    }

    public Path getNewSchema() {
        return newSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
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

    public List<BenchmarkCase> toBenchmarkCases() {
        return List.of(
                new BenchmarkCase(
                        projectId + "-manual-drop-column",
                        "Manual request should recover the evidence chain for " + projectId,
                        new com.schemascope.domain.AnalysisRequest(
                                projectId,
                                projectRoot.toString(),
                                null,
                                null,
                                "DROP_COLUMN",
                                tableName,
                                columnName,
                                null,
                                null,
                                "manual-benchmark"
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                ),
                new BenchmarkCase(
                        projectId + "-schema-diff-drop-column",
                        "Schema diff mode should recover the same chain for " + projectId,
                        new com.schemascope.domain.AnalysisRequest(
                                projectId,
                                projectRoot.toString(),
                                oldSchema.toString(),
                                newSchema.toString(),
                                null,
                                null,
                                null,
                                null,
                                null,
                                null
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                ),
                new BenchmarkCase(
                        projectId + "-manual-drop-table",
                        "Table-level change should recover the main chain for " + projectId,
                        new com.schemascope.domain.AnalysisRequest(
                                projectId,
                                projectRoot.toString(),
                                null,
                                null,
                                "DROP_TABLE",
                                tableName,
                                null,
                                null,
                                null,
                                "manual-benchmark"
                        ),
                        expectedAffectedObjects,
                        expectedDirectObjects,
                        expectedIndirectObjects,
                        true
                )
        );
    }

    public static Set<String> linkedSet(String... values) {
        return new LinkedHashSet<>(List.of(values));
    }
}