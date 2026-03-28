package com.schemascope.benchmark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExternalProjectBenchmarkSupport {

    private ExternalProjectBenchmarkSupport() {
    }

    public static Path requireProjectRoot(String systemPropertyName, String environmentVariableName) {
        String propertyValue = System.getProperty(systemPropertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            Path path = Path.of(propertyValue).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                return path;
            }
            throw new IllegalArgumentException("External project path does not exist: " + path);
        }

        String envValue = System.getenv(environmentVariableName);
        if (envValue != null && !envValue.isBlank()) {
            Path path = Path.of(envValue).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                return path;
            }
            throw new IllegalArgumentException("External project path does not exist: " + path);
        }

        throw new IllegalStateException(
                "Missing external project path. Set -D" + systemPropertyName
                        + "=<projectRoot> or env " + environmentVariableName + "=<projectRoot>"
        );
    }

    public static Path resolvePetClinicSchema(Path projectRoot) {
        Path mysqlSchema = projectRoot.resolve(Path.of("src", "main", "resources", "db", "mysql", "schema.sql"));
        if (Files.exists(mysqlSchema)) {
            return mysqlSchema;
        }

        Path h2Schema = projectRoot.resolve(Path.of("src", "main", "resources", "db", "h2", "schema.sql"));
        if (Files.exists(h2Schema)) {
            return h2Schema;
        }

        throw new IllegalStateException("Could not locate PetClinic schema.sql under db/mysql or db/h2 in " + projectRoot);
    }

    public static Path createDroppedColumnVariant(Path originalSchema,
                                                  String tableName,
                                                  String columnName) throws IOException {
        String original = Files.readString(originalSchema, StandardCharsets.UTF_8);

        String withoutColumn = original.replaceAll(
                "(?im)^\\s*" + java.util.regex.Pattern.quote(columnName) + "\\s+[^,\\n]*,?\\s*$",
                ""
        );

        Path tempFile = Files.createTempFile("schemascope-external-", "-schema-v2.sql");
        Files.writeString(tempFile, withoutColumn, StandardCharsets.UTF_8);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    public static BenchmarkCase buildManualDropColumnCase(Path projectRoot) {
        return new BenchmarkCase(
                "spring-petclinic-manual-drop-column",
                "External PetClinic manual request should surface owner repository/controller impact",
                new com.schemascope.domain.AnalysisRequest(
                        "spring-petclinic",
                        projectRoot.toString(),
                        null,
                        null,
                        "DROP_COLUMN",
                        "owners",
                        "last_name",
                        "VARCHAR(30)",
                        null,
                        "external-benchmark"
                ),
                BenchmarkProjectSpec.linkedSet(
                        "OwnerRepository",
                        "OwnerController"
                ),
                BenchmarkProjectSpec.linkedSet("OwnerRepository"),
                BenchmarkProjectSpec.linkedSet("OwnerController"),
                true
        );
    }

    public static BenchmarkCase buildSchemaDiffDropColumnCase(Path projectRoot,
                                                              Path oldSchema,
                                                              Path newSchema) {
        return new BenchmarkCase(
                "spring-petclinic-schema-diff-drop-column",
                "External PetClinic schema diff should surface owner repository/controller impact",
                new com.schemascope.domain.AnalysisRequest(
                        "spring-petclinic",
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
                BenchmarkProjectSpec.linkedSet(
                        "OwnerRepository",
                        "OwnerController"
                ),
                BenchmarkProjectSpec.linkedSet("OwnerRepository"),
                BenchmarkProjectSpec.linkedSet("OwnerController"),
                true
        );
    }
}